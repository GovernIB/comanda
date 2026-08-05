package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltre;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de gestió i consulta dels filtres de capçalera de dashboards.
 *
 * @author Límit Tecnologies
 */
@RestController("dashboardFiltreController")
@RequestMapping(BaseConfig.API_PATH + "/dashboardFiltres")
@Tag(name = "14. DashboardFiltre", description = "Servei de gestió de filtres de capçalera de dashboards")
public class DashboardFiltreController extends BaseMutableResourceController<DashboardFiltre, Long> {

}
