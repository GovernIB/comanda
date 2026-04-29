package es.caib.comanda.estadistica.persist.entity.paleta;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Plantilla;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = BaseConfig.DB_PREFIX + "plantilla")
public class PlantillaEntity extends BaseEntity<Plantilla> {

    @Column(name = "nom")
    private String nom;
    @Column(name = "mostrar_vora")
    private Boolean mostrarVora;
    @Column(name = "ample_vora")
    private Integer ampleVora;
    @Column(name = "mida_font_titol")
    private String midaFontTitol;
    @Column(name = "mida_font_descripcio")
    private String midaFontDescripcio;
    @Column(name = "icona")
    private String icona;
    @Column(name = "mida_font_valor")
    private Integer midaFontValor;
    @Column(name = "mida_font_unitats")
    private Integer midaFontUnitats;
    @Column(name = "mida_font_canvi_percentual")
    private Integer midaFontCanviPercentual;
    @Column(name = "mostrar_reticula")
    private Boolean mostrarReticula;
    @Column(name = "bar_stacked")
    private Boolean barStacked;
    @Column(name = "bar_horizontal")
    private Boolean barHorizontal;
    @Column(name = "line_show_points")
    private Boolean lineShowPoints;
    @Column(name = "area")
    private Boolean area;
    @Column(name = "line_smooth")
    private Boolean lineSmooth;
    @Column(name = "line_width")
    private Integer lineWidth;
    @Column(name = "outer_radius")
    private Integer outerRadius;
    @Column(name = "pie_donut")
    private Boolean pieDonut;
    @Column(name = "inner_radius")
    private Integer innerRadius;
    @Column(name = "pie_show_labels")
    private Boolean pieShowLabels;
    @Column(name = "label_size")
    private Integer labelSize;
    @Column(name = "gauge_min")
    private Integer gaugeMin;
    @Column(name = "gauge_max")
    private Integer gaugeMax;
    @Column(name = "gauge_rangs")
    private String gaugeRangs;
    @Column(name = "heatmap_min_value")
    private Integer heatmapMinValue;
    @Column(name = "heatmap_max_value")
    private Integer heatmapMaxValue;
    @Column(name = "mostrar_capcalera")
    private Boolean mostrarCapcalera;
    @Column(name = "mostrar_alternancia")
    private Boolean mostrarAlternancia;
    @Column(name = "mostrar_vora_taula")
    private Boolean mostrarVoraTaula;
    @Column(name = "ample_vora_taula")
    private Integer ampleVoraTaula;
    @Column(name = "mostrar_separador_horitzontal")
    private Boolean mostrarSeparadorHoritzontal;
    @Column(name = "ample_separador_horitzontal")
    private Integer ampleSeparadorHoritzontal;
    @Column(name = "mostrar_separador_vertical")
    private Boolean mostrarSeparadorVertical;
    @Column(name = "ample_separador_vertical")
    private Integer ampleSeparadorVertical;

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaletaEntity> paletes;
}
