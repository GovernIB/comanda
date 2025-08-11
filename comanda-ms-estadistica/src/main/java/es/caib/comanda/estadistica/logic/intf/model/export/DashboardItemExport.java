package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Classe per exportar un element dins d'un quadre de comandament (Dashboard).
 *
 *  @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardItemExport implements Serializable {

    private String appCodi;
    private String entornCodi;
    private EstadisticaWidgetExport widget;

    private int posX;
    private int posY;
    private int width;
    private int height;
    private AtributsVisuals atributsVisuals;

}
