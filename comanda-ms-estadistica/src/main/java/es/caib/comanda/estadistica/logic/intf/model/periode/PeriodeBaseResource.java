package es.caib.comanda.estadistica.logic.intf.model.periode;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
public class PeriodeBaseResource<ID extends Serializable> extends BaseResource<ID> {

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
}
