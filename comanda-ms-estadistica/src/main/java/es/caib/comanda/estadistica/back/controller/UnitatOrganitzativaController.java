package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de unitatOrganitzatives estadístiques.
 *
 * @author Límit Tecnologies
 */
@RestController("unitatOrganitzativaController")
@RequestMapping(BaseConfig.API_PATH + "/unitatOrganitzatives")
@Tag(name = "?. UnitatOrganitzativa", description = "Servei de consulta de unitatOrganitzatives")
public class UnitatOrganitzativaController extends BaseMutableResourceController<UnitatOrganitzativa, Long> {

}
