package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.TemplateEstils;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST per a la gestió de plantilles d'estils.
 */
@RestController("templateEstilsController")
@RequestMapping(BaseConfig.API_PATH + "/templateEstils")
@Tag(name = "TemplateEstils", description = "Gestió de plantilles d'estils")
public class TemplateEstilsController extends BaseMutableResourceController<TemplateEstils, Long> {
}
