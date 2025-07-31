package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Classe per exportar un IndicadorTaula.
 *
 * @author Límit Tecnologies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorTaulaExport implements Serializable {

    private String indicadorCodi;
    private TableColumnsEnum agregacio;
    private PeriodeUnitat unitatAgregacio;
    private String titol;

}
