package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de widgets gràfics.
 *
 * @author Límit Tecnologies
 */
@RestController("estadisticaGraficWidgetController")
@RequestMapping(BaseConfig.API_PATH + "/widgetsGrafic")
@Tag(name = "Dimensio", description = "Servei de consulta de widgets gràfics")
public class EstadisticaGraficWidgetController extends BaseMutableResourceController<EstadisticaGraficWidget, Long> {

}
