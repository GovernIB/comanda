package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.estadistica.logic.intf.model.Dimensio;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@RestController("dimensioController")
@RequestMapping(BaseConfig.API_PATH + "/dimensions")
@Tag(name = "Dimensio", description = "Servei de consulta de dimensions")
public class DimensioController extends BaseReadonlyResourceController<Dimensio, Long> {

}
