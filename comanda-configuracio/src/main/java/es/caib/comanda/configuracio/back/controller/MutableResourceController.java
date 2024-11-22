package es.caib.comanda.configuracio.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.configuracio.logic.intf.model.OnChangeEvent;
import es.caib.comanda.configuracio.logic.intf.model.Resource;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.Serializable;

/**
 * Mètodes dels controladors de l'API REST per a modificar un recurs de l'aplicació.
 *
 * @author Limit Tecnologies
 */
public interface MutableResourceController<R extends Resource<? extends Serializable>, ID extends Serializable>
		extends ReadonlyResourceController<R, ID> {

	/**
	 * Crea un nou recurs amb la informació especificada.
	 *
	 * @param resource
	 *            informació del recurs.
	 * @return el recurs creat.
	 */
	ResponseEntity<EntityModel<R>> create(
			final R resource);

	/**
	 * Modifica un recurs existent amb la informació especificada.
	 *
	 * @param resourceId
	 *            id de l'element que es vol modificar.
	 * @param resource
	 *            informació del recurs.
	 * @param bindingResult
	 *            instància de BindingResult per a validar l'element.
	 * @return el recurs modificat.
	 * @throws MethodArgumentNotValidException
	 *            si s'envia una modificació errònia o no permesa.
	 */
	ResponseEntity<EntityModel<R>> update(
			final ID resourceId,
			final R resource,
			BindingResult bindingResult) throws MethodArgumentNotValidException;

	/**
	 * Modifica un recurs existent amb la informació especificada.
	 *
	 * @param resourceId
	 *            id de l'element que es vol modificar.
	 * @param jsonNode
	 *            camps del recurs que s'han de modificar.
	 * @param bindingResult
	 *            instància de BindingResult per a validar l'element.
	 * @return el recurs modificat.
	 * @throws JsonProcessingException
	 *            si es produeixen errors al parsejar els camps.
	 * @throws MethodArgumentNotValidException
	 *            si s'envia una modificació errònia o no permesa.
	 */
	ResponseEntity<EntityModel<R>> patch(
			final ID resourceId,
			final JsonNode jsonNode,
			final BindingResult bindingResult) throws JsonProcessingException, MethodArgumentNotValidException;

	/**
	 * Esborra un recurs existent.
	 *
	 * @param resourceId
	 *            id de l'element que es vol esborrar.
	 * @return HTTP 200 si tot ha anat be.
	 */
	ResponseEntity<?> delete(
			final ID resourceId);


	/**
	 * Processament en el backend dels canvis en els camps dels recursos que
	 * es generen al front.
	 * En aquest mètode no es faran modificacions al recurs sinó que
	 * únicament es processaran els canvis fets en el front. Aquests
	 * canvis es poden propagar com a canvis en altres camps, del
	 * recurs, que es retornaran com a resposta.
	 *
	 * @param onChangeEvent
	 *            informació de l'event onChange.
	 * @return el recurs resultat de processar els canvis.
	 * @throws JsonProcessingException
	 *            si es produeixen errors al parsejar els camps.
	 */
	ResponseEntity<String> onChange(
			final OnChangeEvent onChangeEvent) throws JsonProcessingException;

}