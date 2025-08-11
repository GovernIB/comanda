package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAbsolutTipus;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAlineacio;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAnchor;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeEspecificAny;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class EstadisticaWidgetExport implements Serializable {

    protected String titol;
    protected String descripcio;

    // Dimensions per les que filtrar
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
