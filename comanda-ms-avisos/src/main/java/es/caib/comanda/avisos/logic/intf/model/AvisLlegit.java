package es.caib.comanda.avisos.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
    accessConstraints = {
        @ResourceAccessConstraint(
            type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
            roles = { BaseConfig.ROLE_ADMIN, BaseConfig.ROLE_CONSULTA },
            grantedPermissions = { PermissionEnum.READ }
        )
    }
)
public class AvisLlegit extends BaseResource<Long> {

    @NotNull
    private String usuari;

    @NotNull
    @Transient
    private ResourceReference<Avis, Long> avis;

}
