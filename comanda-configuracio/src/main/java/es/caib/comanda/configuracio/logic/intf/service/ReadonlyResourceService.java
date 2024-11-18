package es.caib.comanda.configuracio.logic.intf.service;

import es.caib.comanda.configuracio.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/**
 * Mètodes a implementar pels serveis que gestionen un recurs en mode només lectura.
 *
 * @param <R> classe del recurs.
 * @param <ID> classe de la clau primària del recurs.
 *
 * @author Limit Tecnologies
 */
public interface ReadonlyResourceService<R extends Resource<? extends Serializable>, ID extends Serializable> {

	/**
	 * Consulta un recurs donada la seva identificació.
	 *
	 * @param id
	 *            clau primària del recurs.
	 * @param perspectives
	 *            llista de perspectives a aplicar.
	 * @return el recurs amb la identificació especificada.
	 * @throws ResourceNotFoundException
	 *             si no s'ha trobat el recurs especificat.
	 */
	R getOne(
			ID id,
			String[] perspectives) throws ResourceNotFoundException;

	/**
	 * Consulta paginada de recursos.
	 *
	 * @param filter
	 *            consulta en format Spring Filter.
	 * @param namedQueries
	 *            llista de noms de consultes a aplicar.
	 * @param perspectives
	 *            llista de perspectives a aplicar.
	 * @param pageable
	 *            paràmetres de paginació i ordenació.
	 * @return la llista de recursos.
	 */
	Page<R> findPage(
			String filter,
			String[] namedQueries,
			String[] perspectives,
			Pageable pageable);

}
