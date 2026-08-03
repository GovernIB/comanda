package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

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
        }
)
public class UnitatOrganitzativa extends BaseResource<Long> {

    private String codi;
    private String denominacio;
    private String nifCif;
    private String codiUnitatSuperior;
    private String codiUnitatArrel;
    private String codiConselleria;
    private UOEstatEnum estat;

    private String codiNom;

}
