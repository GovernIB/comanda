package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.App;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió d'aplicacions.
 *
 * @author Límit Tecnologies
 */
@RestController("appController")
@RequestMapping(BaseConfig.API_PATH + "/apps")
@Tag(name = "Aplicacions", description = "Servei de gestió d'aplicacions")
public class AppController extends BaseMutableResourceController<App, Long> {

}
