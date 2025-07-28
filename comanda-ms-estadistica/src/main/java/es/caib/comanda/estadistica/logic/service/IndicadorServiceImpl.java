package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.estadistica.logic.intf.service.IndicadorService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Objects;

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
@Service
public class IndicadorServiceImpl extends BaseReadonlyResourceService<Indicador, Long, IndicadorEntity> implements IndicadorService {

	@Override
	protected Specification<IndicadorEntity> namedFilterToSpecification(String name) {
		if (Objects.equals(Indicador.NAMED_FILTER_GROUP_BY_NOM, name)) {
			return uniqueNomByMinEntornAppId();
		}
		return null;
	}

	/** Filtro para solo mostrar un resultado por nombre en la aplicación. Aprovechando el UK de entornAppId y nom. **/
	private static Specification<IndicadorEntity> uniqueNomByMinEntornAppId() {
		return (root, query, cb) -> {
			Subquery<Long> subquery = query.subquery(Long.class);
			Root<IndicadorEntity> subRoot = subquery.from(IndicadorEntity.class);
			subquery.select(cb.min(subRoot.get("entornAppId")));
			subquery.where(cb.equal(subRoot.get("nom"), root.get("nom")));
			return cb.equal(root.get("entornAppId"), subquery);
		};
	}

}
