package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Helper per a la comprovació de permisos.
 *
 * @author Límit Tecnologies
 */
@Slf4j
public abstract class BasePermissionHelper {

	@Autowired
	private AuthenticationHelper authenticationHelper;

	/**
	 * Comprova els permisos per a accedir a un recurs.
	 *
	 * @param auth
	 *            l'objecte d'autenticació que s'utilitzarà per a comprovar els permisos.
	 * @param targetId
	 *            l'id del recurs (pot ser null).
	 * @param targetType
	 *            la classe del recurs.
	 * @param permission
	 *            el permís a comprovar (si és null voldrà dir que es comprovarà qualsevol permís).
	 * @return true si l'usuari actual te accés al recurs o false en cas contrari.
	 */
	public boolean checkResourcePermission(
			Authentication auth,
			@Nullable Serializable targetId,
			String targetType,
			@Nullable BasePermission permission) {
		try {
			Class<?> targetTypeClass = Class.forName(targetType);
			ResourceConfig resourceConfig = targetTypeClass.getAnnotation(ResourceConfig.class);
			if (resourceConfig != null) {
				boolean hasPermission = checkResourceAccessConstraints(
						auth,
						targetTypeClass,
						permission,
						resourceConfig.accessConstraints());
				log.debug("Checking {} permission for resource {}: {}", permission, targetType, hasPermission);
				return hasPermission;
			} else {
				// Els recursos sense configuració tenen l'accés permès per defecte
				return true;
			}
		} catch (ClassNotFoundException ex) {
			log.error("Permission denied for resource {} because class not found", targetType, ex);
			return false;
		}
	}

	/**
	 * Comprova els permisos per a accedir a un recurs.
	 *
	 * @param targetId
	 *            l'id del recurs (pot ser null).
	 * @param targetType
	 *            la classe del recurs.
	 * @param permission
	 *            el permís a comprovar (si és null voldrà dir que es comprovarà qualsevol permís).
	 * @return true si l'usuari actual te accés al recurs o false en cas contrari.
	 */
	public boolean checkResourcePermission(
			@Nullable Serializable targetId,
			String targetType,
			@Nullable BasePermission permission) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return checkResourcePermission(
				auth,
				targetId,
				targetType,
				permission);
	}

	protected boolean checkResourceAccessConstraints(
			Authentication auth,
			Class<?> resourceClass,
			BasePermission permission,
			ResourceAccessConstraint[] accessConstraints) {
		if (accessConstraints.length != 0) {
			ResourceAccessConstraint allowedAccessConstraint = Arrays.stream(accessConstraints).
					filter(ac -> {
						boolean accessContraintGranted = false;
						if (ac.type() == ResourceAccessConstraint.ResourceAccessConstraintType.PERMIT_ALL) {
							accessContraintGranted = true;
						} else if (ac.type() == ResourceAccessConstraint.ResourceAccessConstraintType.AUTHENTICATED) {
							accessContraintGranted = auth.isAuthenticated();
						} else if (ac.type() == ResourceAccessConstraint.ResourceAccessConstraintType.ROLE) {
							accessContraintGranted = isCurrentUserInAnyRole(auth, ac.roles());
						} else if (ac.type() == ResourceAccessConstraint.ResourceAccessConstraintType.CUSTOM) {
							accessContraintGranted = checkCustomResourceAccessConstraint(
									auth,
									resourceClass,
									permission);
						}
						if (accessContraintGranted) {
							return permission == null || isPermissionGranted(permission, ac.grantedPermissions());
						} else {
							return false;
						}
					}).
					findFirst().orElse(null);
			return allowedAccessConstraint != null;
		} else {
			// Els recursos sense restriccions d'accés tenen l'accés permès per defecte
			return true;
		}
	}

	protected boolean isCurrentUserInAnyRole(Authentication auth, String[] roles) {
		String firstUserRole = Arrays.stream(roles).
				filter(r -> authenticationHelper.isCurrentUserInRole(auth, r)).
				findFirst().orElse(null);
		return firstUserRole != null;
	}

	protected boolean isPermissionGranted(
			BasePermission permission,
			PermissionEnum[] accessConstraintGrantedPermissions) {
		PermissionEnum firstPermissionGranted = Arrays.stream(accessConstraintGrantedPermissions).
				filter(p -> permission.equals(PermissionEnum.toPermission(p))).
				findFirst().orElse(null);
		return firstPermissionGranted != null;
	}

	protected abstract boolean checkCustomResourceAccessConstraint(
			Authentication auth,
			Class<?> resourceClass,
			BasePermission permission);

}
