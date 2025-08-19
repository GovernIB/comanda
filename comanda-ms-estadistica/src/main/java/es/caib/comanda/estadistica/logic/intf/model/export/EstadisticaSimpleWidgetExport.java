package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Classe per exportar un widget simple d'estadística.
 *
 * @author Límit Tecnologies
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaSimpleWidgetExport extends EstadisticaWidgetExport implements Serializable {

    private IndicadorTaulaExport indicadorInfo;
    private String unitat;
    private boolean compararPeriodeAnterior;
    private AtributsVisualsSimple atributsVisuals;

}
