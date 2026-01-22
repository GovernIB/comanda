package es.caib.comanda.acl.logic.intf.service;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.model.ResourceType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface AclEntryService extends MutableResourceService<AclEntry, String> {

	boolean anyPermissionGranted(
			ResourceType resourceType,
			Serializable resourceId,
			List<PermissionEnum> permissions);

	Set<Serializable> findIdsWithAnyPermission(
			ResourceType resourceType,
			List<PermissionEnum> permissions);

}
