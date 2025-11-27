package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.service.DimensioValorService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
@Service
public class DimensioValorServiceImpl extends BaseMutableResourceService<DimensioValor, Long, DimensioValorEntity> implements DimensioValorService {
    private final SpringFilterHelper springFilterHelper;

    public DimensioValorServiceImpl(SpringFilterHelper springFilterHelper) {
        this.springFilterHelper = springFilterHelper;
    }

    @Override
    protected Specification<DimensioValorEntity> namedFilterToSpecification(String name) {
        if(DimensioValor.NAMED_FILTER_GROUP_BY_VALOR.equals(name))
            return uniqueValorByMinDimensioId();
        return null;
    }

    private static Specification<DimensioValorEntity> uniqueValorByMinDimensioId() {
        return (root, query, cb) -> {
            Subquery<Long> subquery2 = query.subquery(Long.class);
            Root<DimensioValorEntity> subRoot2 = subquery2.from(DimensioValorEntity.class);
            subquery2.select(cb.min(subRoot2.get("dimensioId")));
            subquery2.where(cb.equal(subRoot2.get("valor"), root.get("valor")));
            return cb.equal(root.get("dimensioId"), subquery2);
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
