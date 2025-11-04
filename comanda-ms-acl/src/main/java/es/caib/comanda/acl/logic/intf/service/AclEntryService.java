package es.caib.comanda.acl.logic.intf.service;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.persist.enums.AclAction;
import es.caib.comanda.acl.persist.enums.ResourceType;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

import java.util.List;

public interface AclEntryService extends MutableResourceService<AclEntry, Long> {

    boolean checkPermission(
            String user,
            List<String> roles,
            ResourceType resourceType,
            Long resourceId,
            AclAction action);
}
