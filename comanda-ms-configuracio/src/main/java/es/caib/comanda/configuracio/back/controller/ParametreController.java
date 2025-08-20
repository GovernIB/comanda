package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Parametre;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió de paràmetres.
 *
 * @author Límit Tecnologies
 */
@RestController("parametreController")
@RequestMapping(BaseConfig.API_PATH + "/parametres")
@Tag(name = "Parametres", description = "Servei de gestió de paràmetres")
public class ParametreController extends BaseMutableResourceController<Parametre, Long> {

}
