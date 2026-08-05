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
        quickFilterFields = { "codi", "denominacio" },
        descriptionField = "codiNom",
        accessConstraints = {
            @ResourceAccessConstraint(
                type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                roles = {BaseConfig.ROLE_ADMIN},
                grantedPermissions = {PermissionEnum.READ}
            ),
        },
    artifacts = {
        @ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = UnitatOrganitzativa.PERSP_PERMIS_NUM),
    }
)
public class UnitatOrganitzativa extends BaseResource<Long> {

    public static final String PERSP_PERMIS_NUM = "PERMIS_NUM";

    private String codi;
    private String denominacio;
    private String nifCif;
    private String codiUnitatSuperior;
    private String codiUnitatArrel;
    private String codiConselleria;
    private UOEstatEnum estat;

    private String codiNom;

    @Transient private int numPermisos;

}
