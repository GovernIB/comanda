package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Salut;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@RestController("salutController")
@RequestMapping(BaseConfig.API_PATH + "/saluts")
@Tag(name = "Salut", description = "Servei de consulta d'informació de salut")
public class SalutController extends BaseReadonlyResourceController<Salut, Long> {

}
