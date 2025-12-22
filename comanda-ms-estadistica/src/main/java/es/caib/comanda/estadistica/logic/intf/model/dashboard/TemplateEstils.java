package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Model que representa una plantilla d'estils per als Dashboards i Widgets.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        descriptionField = "nom",
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
                ),
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_CONSULTA },
                        grantedPermissions = { PermissionEnum.READ }
                )
        }
)
public class TemplateEstils extends BaseResource<Long> {

    @NotNull
    @Size(max = 64)
    private String nom;

    // Paleta de colors tema clar (colors separats per comes)
    @Size(max = 1000)
    private String colorsClar;

    // Paleta de colors tema fosc (colors separats per comes)
    @Size(max = 1000)
    private String colorsFosc;

    // Colors destacats clar
    @Size(max = 1000)
    private String destacatsClar;

    // Colors destacats fosc
    @Size(max = 1000)
    private String destacatsFosc;

    // Estils per defecte dels widgets en format JSON
    @Size(max = 4000)
    private String estilsDefaultJson;
}
