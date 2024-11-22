package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.model.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * Mètodes dels controladors de l'API REST per a consultar un recurs de l'aplicació.
 * 
 * @author Limit Tecnologies
 */
public interface ReadonlyResourceController<R extends Resource<? extends Serializable>, ID extends Serializable> {

	/**
	 * Retorna un recurs donat el seu id.
	 * 
	 * @param resourceId
	 *            id de l'element que es vol consultar.
	 * @param perspectives
	 *            la llista de perspectives a aplicar.
	 * @return la informació del recurs.
	 */
	ResponseEntity<EntityModel<R>> getOne(
			final ID resourceId,
			final String[] perspectives);

	/**
	 * Consulta paginada de recursos.
	 * 
	 * @param quickFilter
	 *            text per a filtrar múltiples camps.
	 * @param filter
	 *            consulta en format Spring Filter.
	 * @param namedQueries
	 *            la llista de noms de consultes a aplicar.
	 * @param perspectives
	 *            la llista de perspectives a aplicar.
	 * @param pageable
	 *            informació sobre la pagina de resultats que es vol obtenir.
	 * @return la llista de camps.
	 */
	ResponseEntity<PagedModel<EntityModel<R>>> find(
			final String quickFilter,
			final String filter,
			final String[] namedQueries,
			final String[] perspectives,
			final Pageable pageable);

}