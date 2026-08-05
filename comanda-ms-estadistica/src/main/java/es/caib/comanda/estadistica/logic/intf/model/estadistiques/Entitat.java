package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

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
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@NoArgsConstructor
@FieldNameConstants
@ResourceConfig(
        quickFilterFields = { "codi", "nom" },
        descriptionField = "codi",
        accessConstraints = {
            @ResourceAccessConstraint(
                type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                roles = { BaseConfig.ROLE_ADMIN },
                grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
            ),
        },
    artifacts = {
        @ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Entitat.PERSP_PERMIS_NUM),
        @ResourceArtifact(type = ResourceArtifactType.ACTION, code = Entitat.ACTION_REFRESH_UO, requiresId = true,
            accessConstraints = {
                @ResourceAccessConstraint(
                    type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                    roles = { BaseConfig.ROLE_ADMIN },
                    grantedPermissions = { PermissionEnum.WRITE }
                )
            }),
    }
)
public class Entitat extends BaseResource<Long> {

    public final static String ACTION_REFRESH_UO = "REFRESH_UO";
    public static final String PERSP_PERMIS_NUM = "PERMIS_NUM";

    private String codi;
    private String nom;
    private String codiDir3;

    @Transient
    private int numPermisos;
}
