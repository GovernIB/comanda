package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.widget.AppResource;
import es.caib.comanda.estadistica.logic.intf.model.widget.EntornResource;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Classe que representa un quadre de comandament (Dashboard).
 *
 * Un quadre de comandament conté la informació necessària per organitzar elements visuals (DashboardItems)
 * que proporcionen estadístiques o informes de dades rellevants.
 * Cada Dashboard està vinculat a un conjunt d'elements amb configuracions predefinides dins de les seves propietats.
 *
 * Propietats:
 * - titol: El títol del quadre de comandament, limitat a 64 caràcters.
 * - descripcio: Una descripció opcional del quadre de comandament, limitada a 1024 caràcters.
 * - items: Una llista d'elements (DashboardItem) que defineixen els components visuals del Dashboard.
 *
 * Aquesta classe hereta de BaseResource i inclou un identificador únic del tipus Long proporcionat per la classe base.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "titol",
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
        },
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.ACTION, code = Dashboard.CLONE_ACTION, requiresId = true, formClass = Dashboard.class),
                @ResourceArtifact(type = ResourceArtifactType.REPORT, code = Dashboard.WIDGETS_REPORT, requiresId = true),
                @ResourceArtifact(type = ResourceArtifactType.REPORT, code = Dashboard.DASHBOARD_EXPORT, requiresId = true)
        }
)
public class Dashboard extends BaseResource<Long> {

    public final static String CLONE_ACTION = "clone_dashboard";
    public final static String WIDGETS_REPORT = "widgets_data";
    public final static String DASHBOARD_EXPORT = "dashboard_export";
    public final static String DASHBOARD_IMPORT = "dashboard_import";

    @NotNull
    @Size(max = 64)
    private String titol;
    @Size(max = 1024)
    private String descripcio;
    protected ResourceReference<AppResource, Long> aplicacio;
    private Long appId;
    protected ResourceReference<EntornResource, Long> entorn;
    private Long entornId;

    private List<DashboardItem> items;
    private List<DashboardTitol> titols;

}
