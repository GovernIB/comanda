package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.estadistica.logic.intf.model.Fet;
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
@RestController("fetController")
@RequestMapping(BaseConfig.API_PATH + "/fets")
@Tag(name = "Fet", description = "Servei de consulta de fets")
public class FetController extends BaseReadonlyResourceController<Fet, Long> {

}
