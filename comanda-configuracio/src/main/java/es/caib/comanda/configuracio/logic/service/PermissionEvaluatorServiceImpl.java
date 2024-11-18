package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.PermissionHelper;
import es.caib.comanda.configuracio.logic.intf.exception.UnknownPermissionException;
import es.caib.comanda.configuracio.logic.intf.service.PermissionEvaluatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Implementació del servei d'avaluació de permisos.
 * 
 * @author Limit Tecnologies
 */
@Slf4j
@Service("permissionEvaluatorService")
public class PermissionEvaluatorServiceImpl implements PermissionEvaluatorService {

	@Autowired
	private PermissionHelper permissionHelper;

	@Override
	public boolean hasPermission(
			Authentication authentication,
			Object domainObject,
			Object permission) {
		log.debug("Comprovant permisos per a accedir a l'entitat (authentication={}, domainObject={}, permission={})",
				authentication,
				domainObject,
				permission);
		return permissionHelper.checkResourcePermission(
				authentication,
				null,
				domainObject.getClass().getName(),
				toBasePermission(permission));
	}

	@Override
	public boolean hasPermission(
			Authentication authentication,
			Serializable targetId,
			String targetType,
			Object permission) {
		log.debug("Comprovant permisos per a accedir al recurs (authentication={}, targetId={}, targetType={}, permission={})",
				authentication,
				targetId,
				targetType,
				permission);
		return permissionHelper.checkResourcePermission(
				authentication,
				targetId,
				targetType,
				toBasePermission(permission));
	}

	private BasePermission toBasePermission(Object objectPermission) {
		if (objectPermission instanceof RestApiOperation) {
			RestApiOperation restapiOperation = (RestApiOperation)objectPermission;
			switch (restapiOperation) {
				case GET_ONE:
				case FIND:
					return (BasePermission)BasePermission.READ;
				case UPDATE:
				case PATCH:
					return (BasePermission)BasePermission.WRITE;
				case CREATE:
					return (BasePermission)BasePermission.CREATE;
				case DELETE:
					return (BasePermission)BasePermission.DELETE;
			}
		}
		throw new UnknownPermissionException(objectPermission);
	}

}