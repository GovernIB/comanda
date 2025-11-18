package es.caib.comanda.acl.logic.intf.service;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

public interface AclEntryService extends MutableResourceService<AclEntry, String> {

	/*
    boolean checkPermission(
            String user,
            List<String> roles,
            ResourceType resourceType,
            Long resourceId,
            AclAction action);

    // Nova funcionalitat: comprovar múltiples permisos
    boolean checkPermissionsAny(
            String user,
            List<String> roles,
            ResourceType resourceType,
            Long resourceId,
            List<AclAction> actions);

    boolean checkPermissionsAll(
            String user,
            List<String> roles,
            ResourceType resourceType,
            Long resourceId,
            List<AclAction> actions);

    // Alias sol·licitats: isGrantedAny / isGrantedAll
    default boolean isGrantedAny(String user, List<String> roles, ResourceType resourceType, Long resourceId, List<AclAction> actions) {
        return checkPermissionsAny(user, roles, resourceType, resourceId, actions);
    }

    default boolean isGrantedAll(String user, List<String> roles, ResourceType resourceType, Long resourceId, List<AclAction> actions) {
        return checkPermissionsAll(user, roles, resourceType, resourceId, actions);
    }

    // Operacions en bloc sobre entrades ACL
    List<AclEntry> createAll(List<AclEntry> entries);

    List<AclEntry> updateAll(List<AclEntry> entries);

    void deleteAll(List<Long> ids);
	 */

}
