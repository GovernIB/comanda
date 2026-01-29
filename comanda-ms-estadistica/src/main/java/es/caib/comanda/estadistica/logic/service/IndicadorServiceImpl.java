package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.estadistica.logic.intf.service.IndicadorService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
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
 * Classe de servei que implementa la interfície IndicadorService. Aquesta classe proporciona operacions específiques per gestionar
 * els recursos d'Indicador.
 *
 * Estén la classe BaseReadonlyResourceService per heretar funcionalitats comunes de gestió de recursos de només lectura, com ara la
 * recuperació d'informació d'Indicadors des de la base de dades.
 *
 * Utilitza l'annotació @Service, indicant que és un component de servei en el context de Spring.
 * També utilitza @Slf4j per habilitar el registre de logs en aquesta classe.
 *
 * La classe treballa amb entitats d'Indicador (model de negoci), identificadors de tipus Long, i entitats persistents IndicadorEntity.
 * Forma part del mòdul d'estadística dins de l'aplicació comanda.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class IndicadorServiceImpl extends BaseMutableResourceService<Indicador, Long, IndicadorEntity> implements IndicadorService {
    private final SpringFilterHelper springFilterHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;

	@Override
	protected Specification<IndicadorEntity> namedFilterToSpecification(String name) {
		if (name != null && name.startsWith(Indicador.NAMED_FILTER_BY_APP_GROUP_BY_NOM)) {
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
	private static Specification<IndicadorEntity> uniqueNomByMinEntornAppId(List<Long> idsEntornApp) {
        if (idsEntornApp == null || idsEntornApp.isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }
		return (root, query, cb) -> {
			Subquery<Long> subquery = query.subquery(Long.class);
			Root<IndicadorEntity> subRoot = subquery.from(IndicadorEntity.class);
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
                if (namedQuery.contains(Indicador.FILTER_BY_APP_NAMEDFILTER)){
                    long appId = Long.parseLong(namedQuery.split(":")[1]);
                    filters.add(springFilterHelper.filterByApp(appId, Indicador.Fields.entornAppId));
                }
            }
        }
        List<Filter> result = filters.stream().
                filter(f -> f != null && !String.valueOf(f).isEmpty()).
                collect(Collectors.toList());
        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }


}
