package es.caib.comanda.monitor.logic.intf.model.db;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ }
                )
        }
)
public class DbTablespace extends BaseResource<String> {
    private double totalMb;
    private double maxMb;
    private double usatMb;
    private double lliureMb;
    private double pctUsat;
}
