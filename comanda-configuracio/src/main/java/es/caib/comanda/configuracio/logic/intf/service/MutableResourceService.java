package es.caib.comanda.configuracio.logic.intf.service;

import es.caib.comanda.configuracio.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.Resource;

import java.io.Serializable;

/**
 * Mètodes a implementar pels serveis que gestionen un recurs que es pot modificar.
 *
 * @param <R> classe del recurs.
 * @param <ID> classe de la clau primària del recurs.
 *
 * @author Limit Tecnologies
 */
public interface MutableResourceService<R extends Resource<? extends Serializable>, ID extends Serializable>
		extends ReadonlyResourceService<R, ID> {

	/**
	 * Crea un nou recurs.
	 *
	 * @param resource
	 *            informació del recurs.
	 * @return el recurs creat.
	 */
	R create(R resource);

	/**
	 * Actualitza la informació d'un recurs.
	 *
	 * @param id
	 *            identificació del recurs.
	 * @param resource
	 *            informació del recurs.
	 * @return el recurs modificat.
	 * @throws ResourceNotFoundException
	 *             si no s'ha trobat el recurs especificat.
	 */
	R update(
			ID id,
			R resource) throws ResourceNotFoundException;

	/**
	 * Esborra un recurs donat el seu identificador.
	 *
	 * @param id
	 *            identificació del recurs.
	 * @throws ResourceNotFoundException
	 *             si no s'ha trobat el recurs especificat.
	 */
	void delete(ID id) throws ResourceNotFoundException;

}
