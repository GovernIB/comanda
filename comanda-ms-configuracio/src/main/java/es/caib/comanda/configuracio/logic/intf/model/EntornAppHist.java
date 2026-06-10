package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Històric de canvis externs d'entorn d'aplicació.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        descriptionField = "valor",
        quickFilterFields = { "valor" },
        defaultSortFields = {
                @ResourceConfig.ResourceSort(field = "data", direction= Sort.Direction.DESC),
        },
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN, BaseConfig.ROLE_CONSULTA },
                        grantedPermissions = { PermissionEnum.READ }
                ),
        }
)
public class EntornAppHist extends BaseResource<Long> {

    @NotNull
    private String versio;
    @NotNull
    private String revisio;
    @NotNull
    private boolean canviVersio;
    @NotNull
    private LocalDateTime data;

    @NotNull
    @Transient
    @ResourceField(descriptionField = "id")
    protected ResourceReference<EntornApp, Long> entornApp;

}
