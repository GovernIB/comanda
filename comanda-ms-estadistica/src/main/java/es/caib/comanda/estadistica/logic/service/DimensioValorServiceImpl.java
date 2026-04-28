package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.service.DimensioValorService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
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
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat DimensioValor.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives als valors de dimensions,
 * i s'estén de BaseReadonlyResourceService per proporcionar operacions bàsiques en mode només lectura.
 *
 * Les accions específiques d’aquesta implementació estan alineades amb la interfície `DimensioValorService`
 * i gestionen l'accés a les dades mitjançant l'entitat DimensioValorEntity.
 *
 * La classe utilitza el framework Spring per a la gestió de dependències (@Service), i l’anotació @Slf4j per
 * registrar informació de diagnòstic i seguiment.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per oferir serveis relacionats amb
 * els valors associats a dimensions dins del model d'aplicació.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DimensioValorServiceImpl extends BaseMutableResourceService<DimensioValor, Long, DimensioValorEntity> implements DimensioValorService {
    private final SpringFilterHelper springFilterHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;

    @Override
    protected Specification<DimensioValorEntity> namedFilterToSpecification(String name) {
        if (name != null && name.startsWith(DimensioValor.NAMED_FILTER_BY_APP_GROUP_BY_VALOR)) {
            List<Long> idsEntornApp = null;
            String[] parts = name.split(":", 2);
            if (parts.length == 2 && !parts[1].isBlank()) {
                idsEntornApp = estadisticaClientHelper.getEntornAppsIdByAppId(Long.valueOf(parts[1]));
            }
            return uniqueValorByMinEntornAppId(idsEntornApp);
        }
        return null;
    }

    /** Filtro para solo mostrar un resultado por valor en la aplicación.
     * Se requiere aplicar un filtro de entornApps, ya que si no se devolvería un resultado erróneo.
     **/
    private Specification<DimensioValorEntity> uniqueValorByMinEntornAppId(List<Long> idsEntornApp) {
        if (idsEntornApp == null || idsEntornApp.isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<DimensioValorEntity> subRoot = subquery.from(DimensioValorEntity.class);
            subquery.select(cb.min(subRoot.get("dimensio").get("entornAppId")));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(subRoot.get("valor"), root.get("valor")));
            predicates.add(cb.equal(subRoot.get("dimensio").get("nom"), root.get("dimensio").get("nom")));
            predicates.add(cb.equal(subRoot.get("dimensio").get("codi"), root.get("dimensio").get("codi")));
            predicates.add(subRoot.get("dimensio").get("entornAppId").in(idsEntornApp));
            subquery.where(predicates.toArray(new Predicate[0]));
            return cb.equal(root.get("dimensio").get("entornAppId"), subquery);
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
                if (namedQuery.contains(DimensioValor.FILTER_BY_APP_NAMEDFILTER)){
                    long appId = Long.parseLong(namedQuery.split(":")[1]);
                    filters.add(springFilterHelper.filterByApp(appId, DimensioValor.Fields.dimensio + "." + Dimensio.Fields.entornAppId));
                }
            }
        }
        List<Filter> result = filters.stream().
                filter(f -> f != null && !String.valueOf(f).isEmpty()).
                collect(Collectors.toList());
        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }
}
