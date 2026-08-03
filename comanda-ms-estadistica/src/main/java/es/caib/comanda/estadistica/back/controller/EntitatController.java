package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Entitat;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de entitats estadístiques.
 *
 * @author Límit Tecnologies
 */
@RestController("entitatController")
@RequestMapping(BaseConfig.API_PATH + "/entitats")
@Tag(name = "?. Entitat", description = "Servei de consulta de entitats")
public class EntitatController extends BaseMutableResourceController<Entitat, Long> {

}
