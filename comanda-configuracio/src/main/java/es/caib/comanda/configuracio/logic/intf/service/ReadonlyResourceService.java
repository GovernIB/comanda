package es.caib.comanda.configuracio.logic.intf.service;

import es.caib.comanda.configuracio.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.configuracio.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.configuracio.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.configuracio.logic.intf.model.Resource;
import es.caib.comanda.configuracio.logic.intf.model.ResourceArtifact;
import es.caib.comanda.configuracio.logic.intf.model.ResourceArtifactType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

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

	/**
	 * Retorna la llista d'artefactes del tipus especificat als quals l'usuari te accés.
	 *
	 * @param type
	 *            el tipus d'artefacte que es vol consultar (si és null es retornen tots els tipus).
	 * @return la llista dels artefactes permesos.
	 */
	List<ResourceArtifact> artifactGetAllowed(ResourceArtifactType type);

	/**
	 * Retorna la classe que representa el formulari de paràmetres per l'artefacte.
	 *
	 * @param code
	 *            el codi de l'artefacte.
	 * @return la classe que representa el formulari de paràmetres.
	 * @throws ArtifactNotFoundException
	 *             si no es troba l'artefacte amb el codi especificat.
	 */
	Optional<Class<?>> artifactGetFormClass(ResourceArtifactType type, String code) throws ArtifactNotFoundException;

	/**
	 * Genera l'informe amb el codi especificat.
	 *
	 * @param code
	 *            el codi de l'informe.
	 * @param params
	 *            els paràmetres necessaris per a generar l'informe.
	 * @return les dades de l'informe
	 * @param <P> el tipus dels paràmetres.
	 * @throws ArtifactNotFoundException
	 *             si no es troba l'informe amb el codi especificat.
	 * @throws ReportGenerationException
	 *             si es produeix algun error generant l'informe.
	 */
	<P> List<?> reportGenerate(String code, P params) throws ArtifactNotFoundException, ReportGenerationException;

}
