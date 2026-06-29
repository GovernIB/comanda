package es.caib.comanda.monitor.logic.intf.model.db;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE }
                )
        },
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.ACTION, code = DbIndex.ACTION_REBUILD, requiresId = true)
        }
)
public class DbIndex extends BaseResource<String> {

    public static final String ACTION_REBUILD = "REBUILD";

    private String tableName;
    private String status;
    private String uniqueness;
    private long numRows;
    private Date lastAnalyzed;
    private int blevel;
    private long leafBlocks;
}
