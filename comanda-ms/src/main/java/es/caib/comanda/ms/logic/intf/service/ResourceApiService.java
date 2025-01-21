package es.caib.comanda.ms.logic.intf.service;

import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;

import java.io.Serializable;
import java.util.List;

/**
 * Definició del servei de l'API REST.
 * 
 * @author Limit Tecnologies
 */
public interface ResourceApiService {

	/**
	 * Dona d'alta un servei pel recurs especificat.
	 *
	 * @param resourceClass
	 *            la classe del recurs.
	 */
	public void resourceRegister(Class<? extends Resource<?>> resourceClass);

	/**
	 * Retorna una llista dels recursos permesos per l'usuari actual.
	 *
	 * @return la llista de recursos permesos.
	 */
	public List<Class<? extends Resource<?>>> resourceFindAllowed();

	/**
	 * Retorna els permisos de l'usuari actual sobre el recurs especificat.
	 *
	 * @param resourceClass
	 *            la classe del recurs.
	 * @param resourceId
	 *            l'id del recurs.
	 * @return els permisos.
	 */
	public ResourcePermissions permissionsCurrentUser(
			Class<?> resourceClass,
			Serializable resourceId);

}
