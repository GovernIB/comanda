package es.caib.comanda.estadistica.logic.intf.model.periode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Periode {
    
    private PeriodeMode periodeMode;

    // Camps per mode PRESET
    private PresetPeriode presetPeriode;
    private Integer presetCount;

    // Camps per mode RELATIVE
    private PeriodeAnchor relatiuPuntReferencia;
    private Integer relatiuCount;
    private PeriodeUnitat relatiueUnitat;
    private PeriodeAlineacio relatiuAlineacio;

    // Camps per mode ABSOLUT
    private PeriodeAbsolutTipus absolutTipus;
    // Si absolutTipus = DATE_RANGE (Rang de dates)
    private LocalDate absolutDataInici;
    private LocalDate absolutDataFi;
    // Si absolutTipus = SPECIFIC_PERIOD_OF_YEAR (Periode de temps)
    private PeriodeEspecificAny absolutAnyReferencia;
    private Integer absolutAnyValor;
    private PeriodeUnitat absolutPeriodeUnitat;
    private Integer absolutPeriodeInici;
    private Integer absolutPeriodeFi;
    
}
