package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de widgets simples.
 *
 * @author Límit Tecnologies
 */
@RestController("estadisticaSimpleWidgetController")
@RequestMapping(BaseConfig.API_PATH + "/widgetsSimple")
@Tag(name = "Dimensio", description = "Servei de consulta de widgets simples")
public class EstadisticaSimpleWidgetController extends BaseMutableResourceController<EstadisticaSimpleWidget, Long> {

}
