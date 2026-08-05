package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatDadesResultat;
import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatFiltreSql;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.EntitatRepository;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resol la visibilitat de dades d'un dashboard/widget en funció dels permisos d'entitat i òrgan gestor de l'usuari
 * actual (recursos ACL {@code ENTITAT}/{@code UNITAT}). Regles (indicades per l'usuari):
 * <p>
 * - Administradors i usuaris de consulta veuen totes les dades, sense restricció.
 * - Si l'usuari té permisos d'entitat: només veu dades de les entitats sobre les quals té permís.
 * - Si té permisos d'òrgan: només veu dades dels òrgans sobre els quals té permís, i de tots els seus descendents.
 * - Si té permisos d'ambdós tipus: veu la unió (dades de les entitats permeses, MÉS dades dels òrgans permesos,
 *   encara que l'òrgan pertanyi a una entitat sobre la qual no té permís).
 * - Si no té cap permís d'entitat ni d'òrgan: no ha de veure cap dada (vegeu {@link SeguretatDadesResultat#isSensePermisos()}).
 * <p>
 * La restricció només s'aplica a les apps que realment tenen configurada la dimensió ENTITAT i/o ORGAN_GESTOR
 * corresponent - si cap de les dues existeix per a una app concreta, aquesta funcionalitat no li és aplicable.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardSeguretatHelper {

    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;
    private final DimensioRepository dimensioRepository;
    private final EntitatRepository entitatRepository;
    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;
    private final OrganitzativaTreeHelper organitzativaTreeHelper;

    public boolean isExempt() {
        return authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
            || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA);
    }

    /**
     * Entitats que l'usuari actual pot veure a les opcions d'un filtre de dashboard de tipus ENTITAT. Retorna
     * {@code null} si no s'ha d'aplicar cap restricció (usuari administrador/consulta): totes les entitats són
     * opcions vàlides. Si no és exempt, retorna la llista d'entitats sobre les que té permís (buida si no en té
     * cap, i per tant no ha de veure cap opció).
     */
    public List<EntitatEntity> resoldreEntitatsPermeses() {
        if (isExempt()) {
            return null;
        }
        Set<Serializable> entitatIds = getAllowedIds(ResourceType.ENTITAT);
        return entitatRepository.findAllById(toLongIds(entitatIds));
    }

    /**
     * Resol la restricció de seguretat aplicable a les dades d'un widget de l'entorn d'aplicació indicat.
     */
    public SeguretatDadesResultat resoldre(Long entornAppId) {
        if (isExempt()) {
            return SeguretatDadesResultat.builder().exempt(true).build();
        }

        Set<Serializable> entitatIds = getAllowedIds(ResourceType.ENTITAT);
        Set<Serializable> unitatIds = getAllowedIds(ResourceType.UNITAT);
        if (entitatIds.isEmpty() && unitatIds.isEmpty()) {
            return SeguretatDadesResultat.builder().sensePermisos(true).build();
        }

        SeguretatFiltreSql.SeguretatFiltreSqlBuilder filtre = SeguretatFiltreSql.builder();
        boolean algunaDimensioAplicable = false;

        Optional<DimensioEntity> dimensioEntitat = dimensioRepository.findByEntornAppIdAndTipus(entornAppId, TipusDimensioEnum.ENTITAT);
        if (dimensioEntitat.isPresent()) {
            algunaDimensioAplicable = true;
            filtre.dimensioEntitatCodi(dimensioEntitat.get().getCodi());
            filtre.valorsEntitatPermesos(resoldreCodisEntitatsPermeses(entitatIds, dimensioEntitat.get()));
        }

        Optional<DimensioEntity> dimensioOrgan = dimensioRepository.findByEntornAppIdAndTipus(entornAppId, TipusDimensioEnum.ORGAN_GESTOR);
        if (dimensioOrgan.isPresent()) {
            algunaDimensioAplicable = true;
            filtre.dimensioOrganCodi(dimensioOrgan.get().getCodi());
            filtre.valorsOrganPermesos(resoldreCodisOrgansPermesosAmbDescendents(unitatIds));
        }

        if (!algunaDimensioAplicable) {
            // Aquesta app no té ni dimensió ENTITAT ni ORGAN_GESTOR: la restricció no li és aplicable.
            return SeguretatDadesResultat.builder().exempt(true).build();
        }
        return SeguretatDadesResultat.builder().filtreSql(filtre.build()).build();
    }

    private List<String> resoldreCodisEntitatsPermeses(Set<Serializable> entitatIds, DimensioEntity dimensioEntitat) {
        if (entitatIds.isEmpty()) {
            return List.of();
        }
        List<EntitatEntity> entitats = entitatRepository.findAllById(toLongIds(entitatIds));
        boolean usaCodiDir3 = EntitatValorTipus.CODI_DIR3.equals(dimensioEntitat.getEntitatValorTipus());
        return entitats.stream()
            .map(e -> usaCodiDir3 ? e.getCodiDir3() : e.getCodi())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<String> resoldreCodisOrgansPermesosAmbDescendents(Set<Serializable> unitatIds) {
        if (unitatIds.isEmpty()) {
            return List.of();
        }
        List<UnitatOrganitzativaEntity> unitats = unitatOrganitzativaRepository.findAllById(toLongIds(unitatIds));
        return new ArrayList<>(organitzativaTreeHelper.getDescendentsIElMateix(unitats));
    }

    private List<Long> toLongIds(Set<Serializable> ids) {
        return ids.stream().map(id -> Long.valueOf(String.valueOf(id))).collect(Collectors.toList());
    }

    private Set<Serializable> getAllowedIds(ResourceType resourceType) {
        return Optional.ofNullable(aclServiceClient.findIdsWithAnyPermission(
                resourceType,
                List.of(PermissionEnum.READ),
                authenticationHelper.getCurrentUserName(),
                Arrays.asList(authenticationHelper.getCurrentUserRealmRoles()),
                httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
            .orElse(Collections.emptySet());
    }

}
