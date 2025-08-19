package es.caib.comanda.permisos.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("permisController")
@RequestMapping(BaseConfig.API_PATH + "/permisos")
@Tag(name = "Permis", description = "Servei de gestió de permisos")
public class PermisController extends BaseMutableResourceController<Permis, Long> {

}
