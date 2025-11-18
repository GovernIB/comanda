package es.caib.comanda.acl.logic.helper;

import es.caib.comanda.acl.logic.config.AclConfig;
import es.caib.comanda.ms.logic.intf.permission.ExtendedPermission;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper encarregat de gestionar els permisos amb ACLs.
 *
 * @author Limit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AclHelper {

	private final AclConfig aclConfig;
	private final MutableAclService mutableAclService;

	public Acl get(
			Class<?> resourceClass,
			Serializable resourceId,
			List<Sid> sids) {
		return getMutableAcl(
				resourceClass,
				resourceId,
				sids,
				false);
	}

	public List<PermissionEnum> set(
			Class<?> resourceClass,
			Serializable resourceId,
			String sidName,
			boolean sidGrantedAuthority,
			List<PermissionEnum> permissionsGranted) {
		Sid sid = getSid(sidName, sidGrantedAuthority);
		MutableAcl acl = getMutableAcl(
				resourceClass,
				resourceId,
				List.of(sid),
				true);
		// Es recorren els permisos de l'ACL i s'esborren els que no
		// hi han de ser. Els permisos de permissionList que ja hi son
		// S'esborren de la llista.
		// Es recorren girats perque cada vegada que s'esborra un ace
		// es reorganitzen els índexos
		List<Permission> permissionList = permissionsGranted.stream().
				map(ExtendedPermission::fromEnumValue).
				collect(Collectors.toList());
		for (int i = acl.getEntries().size() - 1; i >= 0; i--) {
			AccessControlEntry ace = acl.getEntries().get(i);
			if (ace.getSid().equals(sid)) {
				if (permissionList.contains(ace.getPermission())) {
					permissionList.remove(ace.getPermission());
				} else {
					acl.deleteAce(i);
				}
			}
		}
		// S'afegeixen els permisos que queden a la llista
		for (Permission permissionItem: permissionList) {
			acl.insertAce(
					acl.getEntries().size(),
					permissionItem,
					sid,
					true);
		}
		mutableAclService.updateAcl(acl);
		return getResourcePermissionsList(
				resourceClass,
				resourceId,
				sid);
	}

	public void delete(
			Class<?> resourceClass,
			Serializable resourceId,
			String sidName,
			boolean sidGrantedAuthority) {
		set(
				resourceClass,
				resourceId,
				sidName,
				sidGrantedAuthority,
				new ArrayList<>());
	}

	public List<Sid> getCurrentUserSids() {
		List<Sid> sids = new ArrayList<>();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			sids.add(new PrincipalSid(auth.getName()));
			for (GrantedAuthority ga: auth.getAuthorities()) {
				sids.add(new GrantedAuthoritySid(ga.getAuthority()));
			}
		}
		return sids;
	}

	public List<PermissionEnum> getResourcePermissionsList(
			Class<?> resourceClass,
			Serializable resourceId,
			Sid... sids) {
		List<PermissionEnum> permissions = new ArrayList<>();
		MutableAcl acl = getMutableAcl(
				resourceClass,
				resourceId,
				Arrays.asList(sids),
				false);
		if (acl != null) {
			if (isPermissionGranted(acl, ExtendedPermission.READ, sids)) {
				permissions.add(PermissionEnum.READ);
			}
			if (isPermissionGranted(acl, ExtendedPermission.WRITE, sids)) {
				permissions.add(PermissionEnum.WRITE);
			}
			if (isPermissionGranted(acl, ExtendedPermission.CREATE, sids)) {
				permissions.add(PermissionEnum.CREATE);
			}
			if (isPermissionGranted(acl, ExtendedPermission.DELETE, sids)) {
				permissions.add(PermissionEnum.DELETE);
			}
			if (isPermissionGranted(acl, ExtendedPermission.ADMINISTRATION, sids)) {
				permissions.add(PermissionEnum.ADMINISTRATION);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM0, sids)) {
				permissions.add(PermissionEnum.PERM0);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM1, sids)) {
				permissions.add(PermissionEnum.PERM1);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM2, sids)) {
				permissions.add(PermissionEnum.PERM2);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM3, sids)) {
				permissions.add(PermissionEnum.PERM3);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM4, sids)) {
				permissions.add(PermissionEnum.PERM4);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM5, sids)) {
				permissions.add(PermissionEnum.PERM5);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM6, sids)) {
				permissions.add(PermissionEnum.PERM6);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM7, sids)) {
				permissions.add(PermissionEnum.PERM7);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM8, sids)) {
				permissions.add(PermissionEnum.PERM8);
			}
			if (isPermissionGranted(acl, ExtendedPermission.PERM9, sids)) {
				permissions.add(PermissionEnum.PERM9);
			}
		}
		return permissions;
	}

	private MutableAcl getMutableAcl(
			Class<?> resourceClass,
			Serializable resourceId,
			List<Sid> sids,
			boolean createIfNotExists) {
		ObjectIdentity objectIdentity = new ObjectIdentityImpl(resourceClass.getName(), resourceId);
		MutableAcl acl;
		try {
			acl = (sids != null) ? (MutableAcl)mutableAclService.readAclById(objectIdentity, sids) : (MutableAcl)mutableAclService.readAclById(objectIdentity);
		} catch (NotFoundException ex) {
			if (createIfNotExists) {
				acl = mutableAclService.createAcl(objectIdentity);
				//acl.setParent(newParent);
				//acl.setEntriesInheriting(true);
			} else {
				acl = null;
			}
		}
		return acl;
	}

	private boolean isPermissionGranted(
			Acl acl,
			Permission permission,
			Sid... sids) {
		try {
			return acl.isGranted(
					Collections.singletonList(permission),
					Arrays.asList(sids),
					true);
		} catch (NotFoundException ex) {
			return false;
		}
	}

	private Sid getSid(
			String name,
			boolean grantedAuthority) {
		if (!grantedAuthority) {
			return new PrincipalSid(name);
		} else {
			return new GrantedAuthoritySid(name);
		}
	}

}
