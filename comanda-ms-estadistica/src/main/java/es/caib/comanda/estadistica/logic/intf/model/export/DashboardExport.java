package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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

    @NotBlank
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity.TITOL_MAX_LENGTH)
    private String titol;

    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity.DESCRIPCIO_MAX_LENGTH)
    private String descripcio;

    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorFonsClar;
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorFonsFosc;

    private String entornCodi;
    private String appCodi;

    @Valid
    private PlantillaExport plantilla;

    @Valid
    private List<DashboardItemExport> items;

    @Valid
    private List<DashboardTitolExport> titols;

    // Indicadors utilitzats pels widgets d'aquest dashboard (vegeu IndicadorExport). Permet que, en importar-lo
    // a un altre entorn, els indicadors de tipus FORMULA es puguin crear automàticament si encara no hi existeixen.
    @Valid
    private List<IndicadorExport> indicadors;

}
