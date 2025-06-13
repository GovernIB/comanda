package es.caib.comanda.estadistica.logic.intf.model.consulta;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InformeWidgetGraficItem extends InformeWidgetItem implements Serializable {

    private TipusGraficEnum tipusGrafic;
    private TipusGraficDataEnum tipusDades;
//    private GraficValueTypeEnum tipusValors;

    // Data for the chart
    private List<Map<String, String>> labels;
    private List<Map<String, Object>> dades;
    private String columnaAgregacio;

    private String llegendaX;
//    private String llegendaY;
//    // For charts with dimension decomposition
//    private List<String> dimensionValues;

    // Configuracions visuals
    private AtributsVisualsGrafic atributsVisuals;
}
