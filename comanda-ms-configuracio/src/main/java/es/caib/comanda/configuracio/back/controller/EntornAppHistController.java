package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.EntornAppHist;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió d'històric de canvis externs d'entorn d'aplicació.
 *
 * @author Límit Tecnologies
 */
@RestController("entornAppHistController")
@RequestMapping(BaseConfig.API_PATH + "/entornAppHist")
@Tag(name = "06. Entorn-App-Historic", description = "Servei de gestió d'històric de canvis externs d'entorn d'aplicació.")
public class EntornAppHistController extends BaseMutableResourceController<EntornAppHist, Long> {

}
