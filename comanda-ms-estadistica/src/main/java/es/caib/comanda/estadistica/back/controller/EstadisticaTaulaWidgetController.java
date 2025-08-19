package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de widgets tipus taula.
 *
 * @author Límit Tecnologies
 */
@RestController("estadisticaTaulaWidgetController")
@RequestMapping(BaseConfig.API_PATH + "/widgetsTaula")
@Tag(name = "Dimensio", description = "Servei de consulta de widgets tipus taula")
public class EstadisticaTaulaWidgetController extends BaseMutableResourceController<EstadisticaTaulaWidget, Long> {

}
