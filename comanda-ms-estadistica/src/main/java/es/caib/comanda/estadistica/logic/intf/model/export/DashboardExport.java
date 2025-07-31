package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Classe per exportar un quadre de comandament (Dashboard).
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardExport implements Serializable {

    private String titol;
    private String descripcio;
    private String entornCodi;
    private String appCodi;
    private List<DashboardItemExport> items;
    private List<DashboardTitolExport> titols;

}
