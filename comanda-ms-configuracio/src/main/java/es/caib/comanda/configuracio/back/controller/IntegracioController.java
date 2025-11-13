package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Integracio;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió d'integracions.
 *
 * @author Límit Tecnologies
 */
@RestController("integracioController")
@RequestMapping(BaseConfig.API_PATH + "/integracions")
@Tag(name = "04. Integracions", description = "Servei de gestió d'integracions")
public class IntegracioController extends BaseMutableResourceController<Integracio, Long> {

}
