package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaTaulaWidget;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de widgets tipus taula.
 *
 * @author Límit Tecnologies
 */
@RestController("estadisticaTaulaWidgetController")
@RequestMapping(BaseConfig.API_PATH + "/widgets/taules")
@Tag(name = "Dimensio", description = "Servei de consulta de widgets tipus taula")
public class EstadisticaTaulaWidgetController extends BaseReadonlyResourceController<EstadisticaTaulaWidget, Long> {

}
