package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.scheduler.TascaBackground;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("tascaBackgroundController")
@RequestMapping(BaseConfig.API_PATH + "/tasquesBackground")
@Tag(name = "18. Tasques en segon pla", description = "Servei de gestió de tasques en segon pla")
public class TascaBackgroundController extends BaseMutableResourceController<TascaBackground, String> {
}
