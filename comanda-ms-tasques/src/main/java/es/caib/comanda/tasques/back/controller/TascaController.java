package es.caib.comanda.tasques.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("tascaController")
@RequestMapping(BaseConfig.API_PATH + "/tasques")
@Tag(name = "Tasca", description = "Servei de gestió de tasques")
public class TascaController extends BaseMutableResourceController<Tasca, Long> {

}
