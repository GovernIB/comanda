package es.caib.comanda.estadistica.logic.intf.model;

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

    private String titol;
    private String llegendaX;
    private String llegendaY;
    private TipusGraficEnum tipusGrafic;
    private GraficValueTypeEnum tipusValors;

    // Data for the chart
    private List<String> labels;
    private List<Map<String, Object>> series;

    // For charts with dimension decomposition
    private List<String> dimensionValues;

    // Configuracions visuals
    private AtributsVisualsGrafic atributsVisuals;
}
