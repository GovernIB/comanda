package es.caib.comanda.estadistica.logic.intf.model.paleta;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        quickFilterFields = { "nom" },
        descriptionField = "nom"
)
public class Plantilla extends BaseResource<Long> {

    @NotNull
    private String nom;
    private Boolean mostrarVora;
    private Integer ampleVora;
    private String midaFontTitol;
    private String midaFontDescripcio;
    private String icona;
    private Integer midaFontValor;
    private Integer midaFontUnitats;
    private Integer midaFontCanviPercentual;
    private Boolean mostrarReticula;
    private Boolean barStacked;
    private Boolean barHorizontal;
    private Boolean lineShowPoints;
    private Boolean area;
    private Boolean lineSmooth;
    private Integer lineWidth;
    private Integer outerRadius;
    private Boolean pieDonut;
    private Integer innerRadius;
    private Boolean pieShowLabels;
    private Integer labelSize;
    private Integer gaugeMin;
    private Integer gaugeMax;
    private String gaugeRangs;
    private Integer heatmapMinValue;
    private Integer heatmapMaxValue;
    private Boolean mostrarCapcalera;
    private Boolean mostrarAlternancia;
    private Boolean mostrarVoraTaula;
    private Integer ampleVoraTaula;
    private Boolean mostrarSeparadorHoritzontal;
    private Integer ampleSeparadorHoritzontal;
    private Boolean mostrarSeparadorVertical;
    private Integer ampleSeparadorVertical;

    @Transient private TipusGraficEnum tipusGrafic;
    @Transient private Map<String, String> colors;
    @Transient private List<Paleta> paletes;
    @Transient private List<DashboardTemplatePaletteGroup> paletteGroups;
    @Transient private List<WidgetStyleProperty> styleProperties;
}
