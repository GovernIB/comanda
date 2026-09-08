package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UOEstatEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ORGANIGRAMA_CACHE;
import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ORG_TREE_CACHE;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnitatOrganitzativaHelper {

    private final UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;
    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;
    private final ResourceEntityMappingHelper resourceEntityMappingHelper;
    private final AclServiceClient aclServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    /**
     * Llista (cachejada) de totes les unitats organitzatives d'un arrel Dir3, per a la pantalla d'organigrama.
     * Les dades només canvien amb una sincronització manual de Dir3 (vegeu {@link #evictOrganigramaCache}),
     * així que es pot cachejar amb un TTL llarg per evitar recórrer tota la taula a cada obertura de l'organigrama.
     */
    @Cacheable(value = ORGANIGRAMA_CACHE, key = "#codiUnitatArrel")
    public List<UnitatOrganitzativaEntity> findAllByCodiUnitatArrelCached(String codiUnitatArrel) {
        return unitatOrganitzativaRepository.findByCodiUnitatArrel(codiUnitatArrel);
    }

    /** Invalida la cache de l'organigrama d'un arrel (llista i arbre fill-pare) - cal cridar-la després de sincronitzar amb Dir3. */
    @Caching(evict = {
        @CacheEvict(value = ORGANIGRAMA_CACHE, key = "#codiUnitatArrel"),
        @CacheEvict(value = ORG_TREE_CACHE, key = "#codiUnitatArrel")
    })
    public void evictOrganigramaCache(String codiUnitatArrel) {
    }

    /**
     * Construeix la llista de recursos UnitatOrganitzativa d'un arrel per a la pantalla d'organigrama: l'estructura
     * (codi, denominació, jerarquia) prové de la llista cachejada {@link #findAllByCodiUnitatArrelCached}, però el
     * nombre de permisos es calcula sempre en viu (una única crida a l'ACL amb tots els ids), ja que canvia cada
     * cop que un administrador edita els permisos d'una unitat.
     */
    public List<UnitatOrganitzativa> buildOrganigrama(String codiUnitatArrel) {
        List<UnitatOrganitzativaEntity> unitats = findAllByCodiUnitatArrelCached(codiUnitatArrel);
        List<UnitatOrganitzativa> resources = unitats.stream()
            .map(u -> {
                UnitatOrganitzativa r = resourceEntityMappingHelper.entityToResource(u, UnitatOrganitzativa.class);
                r.setDenominacio(u.getDenominacio());
                r.setCodiNom(u.getCodiNom());
                return r;
            })
            .collect(Collectors.toList());

        List<String> ids = unitats.stream().map(u -> String.valueOf(u.getId())).collect(Collectors.toList());
        Map<Serializable, Integer> counts = ids.isEmpty()
            ? Map.of()
            : Optional.ofNullable(aclServiceClient.countAllSidsWithPermission(
                ResourceType.UNITAT, String.join(",", ids), httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
                .orElse(Map.of());

        for (int i = 0; i < resources.size(); i++) {
            resources.get(i).setNumPermisos(counts.getOrDefault(ids.get(i), 0));
        }
        return resources;
    }

    /**
     * Sincronitza les unitats organitzatives d'una entitat des de Dir3 sense propagar cap error - es fa servir en
     * fluxos automàtics (creació d'Entitat, ingesta de fets) que no s'han d'interrompre si Dir3 falla o no està
     * configurat. Per a l'acció manual amb retroalimentació a l'usuari, vegeu {@code Entitat.ACTION_REFRESH_UO}.
     */
    public void refreshFromEntitatCodiDir3(String codiDir3) {
        if (codiDir3 == null || !unitatsOrganitzativesPlugin.isConfigured()) {
            return;
        }
        try {
            updateAll(unitatsOrganitzativesPlugin.findAll(codiDir3));
        } catch (Exception e) {
            log.warn("No s'han pogut sincronitzar les unitats organitzatives des de Dir3 (codiDir3={}): {}", codiDir3, e.getMessage());
        }
    }

    public UnitatOrganitzativaEntity updateByCodi(String codi) throws SistemaExternException {
        UnitatOrganitzativaEntity uo = unitatsOrganitzativesPlugin.findUnidad(codi);
        if (uo.getCodiConselleria() != null && !unitatOrganitzativaRepository.existsByCodi(uo.getCodiConselleria()))
            this.updateByCodi(uo.getCodiConselleria());
        return this.update(uo);
    }

    public UnitatOrganitzativaEntity update(UnitatOrganitzativaEntity uo) {
        Optional<UnitatOrganitzativaEntity> uoExists = unitatOrganitzativaRepository.findByCodi(uo.getCodi());

        if (uoExists.isPresent()) {
            UnitatOrganitzativaEntity u = uoExists.get();
            uoExists.get().update(uo);
            return unitatOrganitzativaRepository.save(u);
        }
        return unitatOrganitzativaRepository.save(uo);
    }

    @Transactional
    public List<UnitatOrganitzativaEntity> updateAll(List<UnitatOrganitzativaEntity> unitats) {
        if (unitats == null || unitats.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> codis = unitats.stream()
            .map(UnitatOrganitzativaEntity::getCodi)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        List<UnitatOrganitzativaEntity> existingList = unitatOrganitzativaRepository.findByCodiIn(codis);
        Map<String, UnitatOrganitzativaEntity> existingMap = existingList.stream()
            .collect(Collectors.toMap(UnitatOrganitzativaEntity::getCodi, Function.identity()));

        List<UnitatOrganitzativaEntity> result = new ArrayList<>(unitats.size());

        for (UnitatOrganitzativaEntity input : unitats) {
            UnitatOrganitzativaEntity target = existingMap.get(input.getCodi());

            if (target != null) {
                target.update(input);
                result.add(target);
            } else {
                if (input.getEstat() == null)
                    input.setEstat(UOEstatEnum.T);

                result.add(input);
            }

            if (input.getDenominacioCa() == null) {
                log.warn("DenominacioCa is null for unitat organitzativa {}", input.getCodi());
                input.setDenominacioCa(input.getDenominacioEs() != null ? input.getDenominacioEs() : "--");
            }
            if (input.getDenominacioEs() == null) {
                log.warn("DenominacioEs is null for unitat organitzativa {}", input.getCodi());
                input.setDenominacioEs(input.getDenominacioCa() != null ? input.getDenominacioCa() : "--");
            }
        }

        return unitatOrganitzativaRepository.saveAll(result);
    }
}
