package es.caib.comanda.configuracio.logic.intf.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.configuracio.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.configuracio.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.OnChangeEvent;
import es.caib.comanda.configuracio.logic.intf.model.Resource;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Map;

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

	/**
	 * Processament en el backend dels canvis en els camps dels recursos que
	 * es generen al front.
	 * En aquest mètode no es faran modificacions al recurs sinó que
	 * únicament es processaran els canvis fets en el front. Aquests
	 * canvis es poden propagar com a canvis en altres camps, del
	 * recurs, que es retornaran com a resposta.
	 *
	 * @param previous
	 *            informació del recurs abans del canvi.
	 * @param fieldName
	 *            nom del camp que s'ha canviat.
	 * @param fieldValue
	 *            el valor del camp que s'ha canviat.
	 * @param answers
	 *            respostes a les preguntes formulades en el front.
	 * @return un map amb els canvis resultants de processar la petició.
	 * @throws AnswerRequiredException
	 *            si es requereix alguna resposta addicional de l'usuari.
	 */
	public Map<String, Object> onChange(
			R previous,
			String fieldName,
			Object fieldValue,
			Map<String, AnswerRequiredException.AnswerValue> answers) throws AnswerRequiredException;

}
