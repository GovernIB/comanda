package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió d'entorns.
 *
 * @author Límit Tecnologies
 */
@RestController("entornController")
@RequestMapping(BaseConfig.API_PATH + "/entorns")
@Tag(name = "Entorns", description = "Servei de gestió d'entorns")
public class EntornController extends BaseMutableResourceController<Entorn, Long> {

}
