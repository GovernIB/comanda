package es.caib.comanda.salut.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.salut.logic.intf.model.Salut;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController("salutController")
@RequestMapping(BaseConfig.API_PATH + "/saluts")
@Tag(name = "06. Salut", description = "Servei de consulta d'informació de salut")
public class SalutController extends BaseReadonlyResourceController<Salut, Long> {

}
