package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Mètodes per a la comprovació de permisos.
 *
 * @author Límit Tecnologies
 */
@Component
public class PermissionHelper extends BasePermissionHelper {

    @Override
    protected boolean checkCustomResourceAccessConstraint(
            Authentication auth,
            Class<?> resourceClass,
            ResourceAccessConstraint resourceAccessConstraint,
            BasePermission permission) {
        return false;
    }

    @Override
    protected boolean checkCustomResourceArtifactAccessConstraint(
            Authentication auth,
            Class<?> resourceClass,
            ResourceArtifactType type,
            String code,
            ResourceAccessConstraint resourceAccessConstraint) {
        return false;
    }

}
