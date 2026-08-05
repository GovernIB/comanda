package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ORG_TREE_CACHE;

/**
 * Resol de manera ràpida els descendents d'una unitat organitzativa dins del seu arbre (identificat per
 * {@code codiUnitatArrel}). Es fa servir per al filtre de seguretat de dashboards: un permís sobre un òrgan
 * s'ha d'estendre a tots els seus òrgans descendents.
 * <p>
 * L'índex fill->pare de cada arbre es manté en cache (vegeu {@link es.caib.comanda.ms.logic.config.HazelCastCacheConfig#ORG_TREE_CACHE}),
 * ja que els arbres només canvien quan s'executa una sincronització manual amb Dir3 ({@code Entitat.ACTION_REFRESH_UO}) -
 * així evitam una consulta a la base de dades (i, encara pitjor, a Dir3) per cada resolució de permisos.
 *
 * @author Límit Tecnologies
 */
@Component
@RequiredArgsConstructor
public class OrganitzativaTreeHelper {

    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;

    /**
     * Índex fill<-pare (codi de pare -> codis de fills directes) de totes les unitats amb la mateixa arrel Dir3.
     * Es construeix amb una única consulta i es manté en cache per arrel.
     */
    @Cacheable(value = ORG_TREE_CACHE, key = "#codiUnitatArrel")
    public Map<String, List<String>> getIndexFills(String codiUnitatArrel) {
        Map<String, List<String>> index = new HashMap<>();
        if (codiUnitatArrel == null) {
            return index;
        }
        for (UnitatOrganitzativaEntity unitat : unitatOrganitzativaRepository.findByCodiUnitatArrel(codiUnitatArrel)) {
            if (unitat.getCodiUnitatSuperior() != null && unitat.getCodi() != null) {
                index.computeIfAbsent(unitat.getCodiUnitatSuperior(), k -> new ArrayList<>()).add(unitat.getCodi());
            }
        }
        return index;
    }

    /**
     * Codi d'una unitat més tots els seus descendents (recursivament), dins de l'arbre indicat.
     */
    public Set<String> getDescendentsIElMateix(String codiUnitatArrel, String codi) {
        Set<String> resultat = new HashSet<>();
        if (codi == null) {
            return resultat;
        }
        Map<String, List<String>> index = getIndexFills(codiUnitatArrel);
        Deque<String> pendents = new ArrayDeque<>();
        pendents.push(codi);
        while (!pendents.isEmpty()) {
            String actual = pendents.pop();
            if (resultat.add(actual)) {
                index.getOrDefault(actual, List.of()).forEach(pendents::push);
            }
        }
        return resultat;
    }

    /**
     * Codis (i descendents) de totes les unitats organitzatives indicades, agrupant per arrel per minimitzar
     * l'ús de la cache (una consulta/entrada de cache per arrel, no per unitat).
     */
    public Set<String> getDescendentsIElMateix(List<UnitatOrganitzativaEntity> unitats) {
        Set<String> resultat = new HashSet<>();
        for (UnitatOrganitzativaEntity unitat : unitats) {
            if (unitat.getCodiUnitatArrel() != null) {
                resultat.addAll(getDescendentsIElMateix(unitat.getCodiUnitatArrel(), unitat.getCodi()));
            } else if (unitat.getCodi() != null) {
                // Sense arrel coneguda (encara no sincronitzada amb Dir3): només ella mateixa.
                resultat.add(unitat.getCodi());
            }
        }
        return resultat;
    }

}
