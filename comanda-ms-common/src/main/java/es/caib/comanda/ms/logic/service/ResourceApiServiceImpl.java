package es.caib.comanda.ms.logic.service;

import es.caib.comanda.ms.logic.helper.PermissionHelper;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.permission.ResourcePermissions;
import es.caib.comanda.ms.logic.intf.service.ResourceApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació del servei de l'API REST.
 * 
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class ResourceApiServiceImpl implements ResourceApiService {

	@Autowired
	private PermissionHelper permissionHelper;

	private final Set<Class<? extends Resource<?>>> registeredResources = new HashSet<>();

	@Override
	public void resourceRegister(Class<? extends Resource<?>> resourceClass) {
		log.info("New resource registered (class={})", resourceClass);
		registeredResources.add(resourceClass);
	}

	@Override
	public List<Class<? extends Resource<?>>> resourceFindAllowed() {
		return new ArrayList<>(registeredResources).stream().
				filter(r -> isResourceAllowed(r, null)).
				sorted(Comparator.comparing(Class::getSimpleName)).
				collect(Collectors.toList());
	}

	@Override
	public ResourcePermissions permissionsCurrentUser(Class<?> resourceClass, Serializable resourceId) {
		boolean isReadGranted = permissionHelper.checkResourcePermission(
				resourceId,
				resourceClass.getName(),
				(BasePermission)BasePermission.READ);
		boolean isWriteGranted = permissionHelper.checkResourcePermission(
				resourceId,
				resourceClass.getName(),
				(BasePermission)BasePermission.WRITE);
		boolean isCreateGranted = permissionHelper.checkResourcePermission(
				resourceId,
				resourceClass.getName(),
				(BasePermission)BasePermission.CREATE);
		boolean isDeleteGranted = permissionHelper.checkResourcePermission(
				resourceId,
				resourceClass.getName(),
				(BasePermission)BasePermission.DELETE);
		return new ResourcePermissions(
				isReadGranted,
				isWriteGranted,
				isCreateGranted,
				isDeleteGranted);
	}

	private boolean isResourceAllowed(
			Class<? extends Resource<?>> resourceClass,
			BasePermission permission) {
		return permissionHelper.checkResourcePermission(
				null,
				resourceClass.getName(),
				permission);
	}

}