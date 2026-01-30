package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.service.DimensioService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat Dimensio.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a Dimensions,
 * i s'estén de BaseReadonlyResourceService per oferir operacions bàsiques de lògica empresarial en mode només lectura.
 *
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície DimensioService
 * i amb l'accés a les dades mitjançant l'entitat DimensioEntity.
 *
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat Dimensio.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DimensioServiceImpl  extends BaseReadonlyResourceService<Dimensio, Long, DimensioEntity> implements DimensioService {
    private final SpringFilterHelper springFilterHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;

    @Override
    protected Specification<DimensioEntity> namedFilterToSpecification(String name) {
        if (name != null && name.startsWith(Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM)) {
            List<Long> idsEntornApp = null;
            String[] parts = name.split(":", 2);
            if (parts.length == 2 && !parts[1].isBlank()) {
                idsEntornApp = estadisticaClientHelper.getEntornAppsIdByAppId(Long.valueOf(parts[1]));
            }
            return uniqueNomByMinEntornAppId(idsEntornApp);
        }
        return null;
    }

    /** Filtro para solo mostrar un resultado por nombre en la aplicación. Aprovechando el UK de entornAppId y nom.
     * Se requiere aplicar un filtro de entornApps, ya que si no se devolvería un resultado erróneo.
     **/
    private static Specification<DimensioEntity> uniqueNomByMinEntornAppId(List<Long> idsEntornApp) {
        if (idsEntornApp == null || idsEntornApp.isEmpty()) { //Si no hay resultados en la lista no devolveremos dimensions
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<DimensioEntity> subRoot = subquery.from(DimensioEntity.class);
            subquery.select(cb.min(subRoot.get("entornAppId")));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(subRoot.get("nom"), root.get("nom")));
            predicates.add(cb.equal(subRoot.get("codi"), root.get("codi")));
            predicates.add(subRoot.get("entornAppId").in(idsEntornApp));
            subquery.where(predicates.toArray(new Predicate[0]));
            return cb.equal(root.get("entornAppId"), subquery);
        };
    }

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
        List<Filter> filters = new ArrayList<>();
        if (currentSpringFilter != null && !currentSpringFilter.isEmpty()) {
            filters.add(Filter.parse(currentSpringFilter));
        }
        if (namedQueries != null) {
            for (String namedQuery : namedQueries) {
                if (namedQuery.contains(Dimensio.FILTER_BY_APP_NAMEDFILTER)){
                    long appId = Long.parseLong(namedQuery.split(":")[1]);
                    filters.add(springFilterHelper.filterByApp(appId, Dimensio.Fields.entornAppId));
                }
            }
        }
        List<Filter> result = filters.stream().
                filter(f -> f != null && !String.valueOf(f).isEmpty()).
                collect(Collectors.toList());
        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }


}
