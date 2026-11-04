package es.caib.comanda.configuracio.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.AppGrup;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei REST de gestió de grups d'aplicacions.
 */
@RestController("appGrupController")
@RequestMapping(BaseConfig.API_PATH + "/appGrups")
@Tag(name = "01.1 Grups d'Aplicacions", description = "Servei de gestió de grups d'aplicacions")
public class AppGrupController extends BaseMutableResourceController<AppGrup, Long> {
}
