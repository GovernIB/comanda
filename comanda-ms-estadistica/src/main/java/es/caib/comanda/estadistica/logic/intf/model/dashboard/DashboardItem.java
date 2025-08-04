package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
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
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.REPORT, code = DashboardItem.WIDGET_REPORT, requiresId = true),
        }
)
public class DashboardItem extends BaseResource<Long> {

    public final static String WIDGET_REPORT = "widget_data";


    @NotNull
    private ResourceReference<Dashboard, Long> dashboard;
    @NotNull
    private ResourceReference<EstadisticaSimpleWidget, Long> widget;
    /** Referencia a entornApp **/
    @NotNull
    private Long entornId;

    @NotNull
    private int posX;
    /** Si es deixa null, es definirà a baix del dashboard. **/
    private Integer posY;
    @NotNull
    private int width;
    @NotNull
    private int height;

    private AtributsVisuals atributsVisuals;

}
