package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Plantilla;
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

/**
 * Classe que representa un element dins d'un quadre de comandament (Dashboard).
 *
 * Un DashboardItem defineix una configuració específica per presentar un component visual dins d'un Dashboard.
 * Conté informació de posició i dimensió, així com les referències al Dashboard al qual pertany
 * i al component visual específic que ha de mostrar.
 *
 * Propietats:
 * - posX: Posició horitzontal de l'element dins del Dashboard.
 * - posY: Posició vertical de l'element dins del Dashboard.
 * - width: Amplada de l'element dins del Dashboard.
 * - height: Alçada de l'element dins del Dashboard.
 * - dashboard: Referència al Dashboard on es troba aquest element.
 * - view: Referència al component visual (EstadisticaSimpleWidget) que aquest element representa.
 *
 * Aquesta classe hereta de BaseResource, utilitzant un identificador únic del tipus Long proporcionat per la classe base.
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
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.AUTHENTICATED,
                        grantedPermissions = { PermissionEnum.READ }
                ),
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
                @ResourceArtifact(type = ResourceArtifactType.ACTION, code = DashboardTitol.DUPLICATE_ACTION, requiresId = true),
        }
)
public class DashboardTitol extends BaseResource<Long> {

    public final static String DUPLICATE_ACTION = "duplicate";
    public final static String WIDGET_REPORT = "widget_data";


    @NotNull
    private ResourceReference<Dashboard, Long> dashboard;

    @NotNull
    private String titol;
    private String subtitol;

    @NotNull
    private int posX;
    /** Si es deixa null, es definirà a baix del dashboard. **/
    private Integer posY;
    @NotNull
    private int width;
    @NotNull
    private int height;

    private DashboardTitolTipus tipusTitol;
    private String colorTitol;
    private Integer midaFontTitol;
    private String colorSubtitol;
    private Integer midaFontSubtitol;
    private String colorFons;
    private PosicioSubtitol posicioSubtitol;
    private Integer separacioSubtitol;
    private Boolean mostrarVoraTop;
    private String colorVoraTop;
    private Integer ampleVoraTop;
    private Boolean mostrarVoraRight;
    private String colorVoraRight;
    private Integer ampleVoraRight;
    private Boolean mostrarVoraBottom;
    private String colorVoraBottom;
    private Integer ampleVoraBottom;
    private Boolean mostrarVoraLeft;
    private String colorVoraLeft;
    private Integer ampleVoraLeft;
    private Boolean destacat;
    private Boolean personalitzat;
    private ResourceReference<Plantilla, Long> plantilla;

}
