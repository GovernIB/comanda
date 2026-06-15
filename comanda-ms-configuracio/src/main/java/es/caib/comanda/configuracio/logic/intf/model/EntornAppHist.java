package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Sort;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
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
        },
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = EntornAppHist.ENTORN_APP_HIST_FILTER, formClass = EntornAppHist.EntornAppHistFilter.class),
        }
)
public class EntornAppHist extends BaseResource<Long> {

    public final static String ENTORN_APP_HIST_FILTER = "entornAppHist_filter";

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

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class EntornAppHistFilter implements Serializable {
        private ResourceReference<EntornApp, Long> entornApp;
        private ResourceReference<Entorn, Long> entorn;
        private ResourceReference<App, Long> app;
        private String versio;
        private String revisio;
        private boolean canviVersio;
        private LocalDate dataDesde;
        private LocalDate dataFins;
    }
}
