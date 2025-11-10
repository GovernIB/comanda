package es.caib.comanda.acl.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.AclEffect;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.ADMINISTRATION }
                ),
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_CONSULTA },
                        grantedPermissions = { PermissionEnum.READ }
                )
        }
)
public class AclEntry extends BaseResource<Long> {

    @NotNull
    private SubjectType subjectType; // USER o ROLE

    @NotBlank
    @Size(max = 128)
    private String subjectValue; // codi d'usuari o nom de rol

    @NotNull
    private ResourceType resourceType; // ENTORN_APP, DASHBOARD, etc

    @NotNull
    private Long resourceId; // id del recurs dins del seu mòdul

    @NotNull
    private AclAction action; // READ/WRITE/ADMIN

    @NotNull
    private AclEffect effect; // ALLOW/DENY
}
