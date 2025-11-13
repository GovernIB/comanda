package es.caib.comanda.permisos.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.permisos.logic.intf.model.Objecte;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("objecteController")
@RequestMapping(BaseConfig.API_PATH + "/objectes")
@Tag(name = "21. Objecte", description = "Servei de gestió d'objectes per permisos")
public class ObjecteController extends BaseMutableResourceController<Objecte, Long> {

}
