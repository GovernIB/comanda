package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Integracio;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió d'integracions.
 *
 * @author Límit Tecnologies
 */
@RestController("integracionController")
@RequestMapping(BaseConfig.API_PATH + "/integracions")
@Tag(name = "Integracions", description = "Servei de gestió d'integracions")
public class IntegracioController extends BaseMutableResourceController<Integracio, Long> {

}
