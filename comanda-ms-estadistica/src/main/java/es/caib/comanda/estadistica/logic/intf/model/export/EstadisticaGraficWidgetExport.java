package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Classe per exportar un widget gràfic estadístic dins del sistema.
 *
 * @author Límit Tecnologies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaGraficWidgetExport extends EstadisticaWidgetExport implements Serializable {

    private TipusGraficEnum tipusGrafic;
    private TipusGraficDataEnum tipusDades;
    private IndicadorTaulaExport indicadorInfo;
    private List<IndicadorTaulaExport> indicadorsInfo;
    private String descomposicioDimensioCodi;
    private Boolean agruparPerDimensioDescomposicio;
    private PeriodeUnitat tempsAgrupacio;
    private String llegendaX;
    private String llegendaY;
    private AtributsVisualsGrafic atributsVisuals;
    private PeriodeUnitat unitatAgregacio;
}
