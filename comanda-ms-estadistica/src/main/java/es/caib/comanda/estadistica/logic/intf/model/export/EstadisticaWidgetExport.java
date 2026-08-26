package es.caib.comanda.estadistica.logic.intf.model.export;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAbsolutTipus;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAlineacio;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAnchor;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeEspecificAny;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Classe per exportar un widget d'estadística tipus taula.
 *
 * @author Límit Tecnologies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tipus",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EstadisticaSimpleWidgetExport.class, name = "SIMPLE"),
        @JsonSubTypes.Type(value = EstadisticaGraficWidgetExport.class, name = "GRAFIC"),
        @JsonSubTypes.Type(value = EstadisticaTaulaWidgetExport.class, name = "TAULA")
})
public class EstadisticaWidgetExport implements Serializable {

    @NotBlank
    @Size(max = es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity.TITOL_MAX_LENGTH)
    protected String titol;

    @Size(max = es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity.DESCRIPCIO_MAX_LENGTH)
    protected String descripcio;

    protected WidgetTipus tipus;

    // Dimensions per les que filtrar
    @javax.validation.Valid
    protected List<DimensioValorExport> dimensionsValor;
    protected PeriodeMode periodeMode;
    protected PresetPeriode presetPeriode;
    protected Integer presetCount;
    protected PeriodeAnchor relatiuPuntReferencia;
    protected Integer relatiuCount;
    protected PeriodeUnitat relatiueUnitat;
    protected PeriodeAlineacio relatiuAlineacio;
    protected PeriodeAbsolutTipus absolutTipus;
    protected LocalDate absolutDataInici;
    protected LocalDate absolutDataFi;
    protected PeriodeEspecificAny absolutAnyReferencia;
    protected Integer absolutAnyValor;
    protected PeriodeUnitat absolutPeriodeUnitat;
    protected Integer absolutPeriodeInici;
    protected Integer absolutPeriodeFi;

}
