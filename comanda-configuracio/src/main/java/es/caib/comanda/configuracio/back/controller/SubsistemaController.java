package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Subsistema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió de subsistemes.
 *
 * @author Límit Tecnologies
 */
@RestController("subsistemaController")
@RequestMapping(BaseConfig.API_PATH + "/subsistemes")
@Tag(name = "Subsistemes", description = "Servei de gestió de subsistemes")
public class SubsistemaController extends BaseMutableResourceController<Subsistema, Long> {

}
