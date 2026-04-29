package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Plantilla;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de plantilla.
 *
 * @author Límit Tecnologies
 */
@RestController("plantillaController")
@RequestMapping(BaseConfig.API_PATH + "/plantilla")
@Tag(name = "?. Plantilla", description = "Servei de consulta de plantilla")
public class PlantillaController extends BaseMutableResourceController<Plantilla, Long> {

}
