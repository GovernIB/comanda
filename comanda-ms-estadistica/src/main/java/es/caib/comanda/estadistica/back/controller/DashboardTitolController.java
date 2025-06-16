package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
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
@RestController("dashboardTitolController")
@RequestMapping(BaseConfig.API_PATH + "/dashboardTitols")
@Tag(name = "DashboardTitol", description = "Servei de consulta de títols de dashboards")
public class DashboardTitolController extends BaseMutableResourceController<DashboardTitol, Long> {

}
