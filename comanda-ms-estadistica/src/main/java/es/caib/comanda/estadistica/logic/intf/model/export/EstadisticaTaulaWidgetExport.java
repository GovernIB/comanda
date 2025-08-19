package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Classe per exportar un widget d'estadística tipus taula.
 *
 * @author Límit Tecnologies
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticaTaulaWidgetExport extends EstadisticaWidgetExport implements Serializable {

    private List<IndicadorTaulaExport> columnes;
    private String dimensioAgrupacioCodi;
    private String titolAgrupament;
    private AtributsVisualsTaula atributsVisuals;

}
