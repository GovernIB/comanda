package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de widgets gràfics.
 *
 * @author Límit Tecnologies
 */
@RestController("dashboardItemController")
@RequestMapping(BaseConfig.API_PATH + "/dashboardItems")
@Tag(name = "Dimensio", description = "Servei de consulta de items de dashboards")
public class DashboardItemController extends BaseMutableResourceController<DashboardItem, Long> {

}
