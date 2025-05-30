package es.caib.comanda.estadistica.logic.intf.model.widget;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.model.periode.Periode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAbsolutTipus;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAlineacio;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAnchor;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeEspecificAny;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WidgetBaseResource<ID extends Serializable> extends BaseResource<ID> {

    @NotNull
    @Size(max = 64)
    protected String titol;
    @Size(max = 1024)
    protected String descripcio;

    @NotNull
    protected Long appId;

    // Dades a emplenar durant la conversió
    protected String aplicacioNom;

    // Dimensions per les que filtrar
    @NotEmpty
    protected List<ResourceReference<DimensioValor, Long>> dimensionsValor;

    @NotNull
    protected PeriodeMode periodeMode;

    // Camps per mode PRESET
    protected PresetPeriode presetPeriode;
    protected Integer presetCount;

    // Camps per mode RELATIVE
    protected PeriodeAnchor relatiuPuntReferencia;
    protected Integer relatiuCount;
    protected PeriodeUnitat relatiueUnitat;
    protected PeriodeAlineacio relatiuAlineacio;

    // Camps per mode ABSOLUT
    protected PeriodeAbsolutTipus absolutTipus;
    // Si absolutTipus = DATE_RANGE (Rang de dates)
    protected LocalDate absolutDataInici;
    protected LocalDate absolutDataFi;
    // Si absolutTipus = SPECIFIC_PERIOD_OF_YEAR (Periode de temps)
    protected PeriodeEspecificAny absolutAnyReferencia;
    protected Integer absolutAnyValor;
    protected PeriodeUnitat absolutPeriodeUnitat;
    protected Integer absolutPeriodeInici;
    protected Integer absolutPeriodeFi;

    public Periode getPeriode() {
        return Periode.builder()
                .periodeMode(this.periodeMode)
                .presetPeriode(this.presetPeriode)
                .presetCount(this.presetCount)
                .relatiuPuntReferencia(this.relatiuPuntReferencia)
                .relatiuCount(this.relatiuCount)
                .relatiueUnitat(this.relatiueUnitat)
                .relatiuAlineacio(this.relatiuAlineacio)
                .absolutTipus(this.absolutTipus)
                .absolutDataInici(this.absolutDataInici)
                .absolutDataFi(this.absolutDataFi)
                .absolutAnyReferencia(this.absolutAnyReferencia)
                .absolutAnyValor(this.absolutAnyValor)
                .absolutPeriodeUnitat(this.absolutPeriodeUnitat)
                .absolutPeriodeInici(this.absolutPeriodeInici)
                .absolutPeriodeFi(this.absolutPeriodeFi)
                .build();
    }
}
