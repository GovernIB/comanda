package es.caib.comanda.configuracio.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.configuracio.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.Resource;
import es.caib.comanda.configuracio.logic.intf.model.ResourceArtifact;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

	/**
	 * Retorna la llista d'artefactes relacionats amb aquest servei.
	 *
	 * @return els artefactes relacionats amb aquest servei.
	 */
	ResponseEntity<CollectionModel<EntityModel<ResourceArtifact>>> artifacts();

	/**
	 * Generació d'un informe associat al recurs.
	 *
	 * @param code
	 *            codi de l'informe a generar.
	 * @param params
	 *            paràmetres per a generar l'informe.
	 * @param bindingResult
	 *            instància de BindingResult per a poder validar els paràmetres.
	 * @return les dades de l'informe.
	 * @throws ArtifactNotFoundException
	 *             si no es troba l'informe amb el codi especificat.
	 * @throws JsonProcessingException
	 *             si es produeix algun error al extreure els paràmetres.
	 * @throws MethodArgumentNotValidException
	 *             si es troben errors de validació en els paràmetres.
	 */
	ResponseEntity<CollectionModel<EntityModel<?>>> artifactReportGenerate(
			final String code,
			final JsonNode params,
			BindingResult bindingResult) throws ArtifactNotFoundException, JsonProcessingException, MethodArgumentNotValidException;

}