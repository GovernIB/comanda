package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Classe que representa la relació de preferència (preferit) entre un usuari i un Dashboard.
 *
 * Aquesta classe utilitza el codi immutable de l'usuari per identificar els dashboards preferits de cada usuari.
 */
@Getter
@Setter
@NoArgsConstructor
@FieldNameConstants
@ResourceConfig(
    descriptionField = "campDescriptiu",
    accessConstraints = {
        @ResourceAccessConstraint(
            type = ResourceAccessConstraint.ResourceAccessConstraintType.AUTHENTICATED,
            grantedPermissions = { PermissionEnum.READ, PermissionEnum.CREATE, PermissionEnum.DELETE }
        )
    }
)
public class DashboardPreferit extends BaseResource<Long> {

    @NotNull
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardPreferitEntity.USUARI_CODI_MAX_LENGTH)
    private String usuariCodi;

    protected ResourceReference<Dashboard, Long> dashboard;
    private Long dashboardId;

    private String getCampDescriptiu(){
        return usuariCodi + " - " + dashboardId;
    }
}
