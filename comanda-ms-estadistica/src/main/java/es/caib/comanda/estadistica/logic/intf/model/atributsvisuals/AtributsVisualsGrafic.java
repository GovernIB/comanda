package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Size;

/**
 * Classe que representa els atributs visuals d'un widget gràfic.
 * Aquesta classe s'utilitza per emmagatzemar els atributs visuals en format JSON.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributsVisualsGrafic implements AtributsVisuals {

    // Colors per a la gama cromàtica del gràfic
    @Size(max = 1000)
    private String colorsPaleta;  // Emmagatzemat com a string amb colors separats per comes

    // Indica si s'ha de mostrar una retícula de fons
    private Boolean mostrarReticula;

    // Configuració específica per a gràfics de tipus BAR_CHART
    private Boolean barStacked;      // Indica si les barres s'han d'apilar
    private Boolean barHorizontal;   // Indica si les barres són horitzontals

    // Configuració específica per a gràfics de tipus LINE_CHART
    private Boolean lineShowPoints;  // Indica si s'han de mostrar els punts a les línies
    private Boolean lineSmooth;      // Indica si les línies han de ser suaus o rectes
    private Integer lineWidth;       // Amplada de les línies

    // Configuració específica per a gràfics de tipus PIE_CHART
    private Boolean pieDonut;        // Indica si el gràfic de pastís és de tipus donut
    private Boolean pieShowLabels;   // Indica si s'han de mostrar les etiquetes al gràfic

    // Configuració específica per a gràfics de tipus GAUGE_CHART
    private Double gaugeMin;         // Valor mínim del gauge
    private Double gaugeMax;         // Valor màxim del gauge
    @Size(max = 1000)
    private String gaugeColors;      // Colors per als diferents rangs del gauge (separats per comes)
    @Size(max = 1000)
    private String gaugeRangs;       // Rangs per als diferents colors del gauge (separats per comes)

    // Configuració específica per a gràfics de tipus HEATMAP_CHART
    @Size(max = 1000)
    private String heatmapColors;    // Colors per al heatmap (separats per comes)
    private Double heatmapMinValue;  // Valor mínim per al heatmap
    private Double heatmapMaxValue;  // Valor màxim per al heatmap


//    public String fromAtributsVisuals() {
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.writeValueAsString(this);
//        } catch (JsonProcessingException e) {
//            log.error("Error convertint atributs visuals a JSON", e);
//            throw new ObjectMappingException(AtributsVisualsGrafic.class, String.class, "Error convertint atributs visuals a JSON");
//        }
//    }
//
//    public static AtributsVisualsGrafic toAtributsVisuals(String json) {
//
//        if (json == null) {
//            return null;
//        }
//
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.readValue(json, AtributsVisualsGrafic.class);
//        } catch (JsonProcessingException e) {
//            log.error("Error convertint JSON a atributs visuals", e);
//            throw new ObjectMappingException(String.class, AtributsVisualsGrafic.class, "Error convertint JSON a atributs visuals");
//        }
//    }

    public AtributsVisuals merge(AtributsVisuals otherAtributsVisuals) {
        if (otherAtributsVisuals == null || !(otherAtributsVisuals instanceof AtributsVisualsGrafic)) {
            return this;
        }

        AtributsVisualsGrafic other = (AtributsVisualsGrafic) otherAtributsVisuals;
        this.colorsPaleta = mergeField(this.colorsPaleta, other.getColorsPaleta());
        this.mostrarReticula = mergeField(this.mostrarReticula, other.getMostrarReticula());
        this.barStacked = mergeField(this.barStacked, other.getBarStacked());
        this.barHorizontal = mergeField(this.barHorizontal, other.getBarHorizontal());
        this.lineShowPoints = mergeField(this.lineShowPoints, other.getLineShowPoints());
        this.lineSmooth = mergeField(this.lineSmooth, other.getLineSmooth());
        this.lineWidth = mergeField(this.lineWidth, other.getLineWidth());
        this.pieDonut = mergeField(this.pieDonut, other.getPieDonut());
        this.pieShowLabels = mergeField(this.pieShowLabels, other.getPieShowLabels());
        this.gaugeMin = mergeField(this.gaugeMin, other.getGaugeMin());
        this.gaugeMax = mergeField(this.gaugeMax, other.getGaugeMax());
        this.gaugeColors = mergeField(this.gaugeColors, other.getGaugeColors());
        this.gaugeRangs = mergeField(this.gaugeRangs, other.getGaugeRangs());
        this.heatmapColors = mergeField(this.heatmapColors, other.getHeatmapColors());
        this.heatmapMinValue = mergeField(this.heatmapMinValue, other.getHeatmapMinValue());
        this.heatmapMaxValue = mergeField(this.heatmapMaxValue, other.getHeatmapMaxValue());

        return this;
    }

}