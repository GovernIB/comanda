package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltreTipus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Classe per exportar un filtre de capçalera dins d'un quadre de comandament (Dashboard).
 *
 * @author Límit Tecnologies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFiltreExport implements Serializable {

    @NotNull
    private DashboardFiltreTipus tipus;

    @Size(max = 32)
    private String dimensioCodi;

    @Size(max = 64)
    private String titol;

    @NotNull
    private int ordre;

    private boolean multiple;
}
