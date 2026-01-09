package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.service.DimensioService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
@Service
public class DimensioServiceImpl  extends BaseReadonlyResourceService<Dimensio, Long, DimensioEntity> implements DimensioService {
    private final SpringFilterHelper springFilterHelper;

    public DimensioServiceImpl(SpringFilterHelper springFilterHelper) {
        this.springFilterHelper = springFilterHelper;
    }

    @Override
    protected Specification<DimensioEntity> namedFilterToSpecification(String name) {
        if (Objects.equals(Dimensio.NAMED_FILTER_GROUP_BY_NOM, name)) {
            return uniqueNomByMinEntornAppId();
        }
        return null;
    }

    /** Filtro para solo mostrar un resultado por nombre en la aplicación. Aprovechando el UK de entornAppId y nom. **/
    private static Specification<DimensioEntity> uniqueNomByMinEntornAppId() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<DimensioEntity> subRoot = subquery.from(DimensioEntity.class);
            subquery.select(cb.min(subRoot.get("entornAppId")));
            subquery.where(cb.equal(subRoot.get("nom"), root.get("nom")), cb.equal(subRoot.get("codi"), root.get("codi")));
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
