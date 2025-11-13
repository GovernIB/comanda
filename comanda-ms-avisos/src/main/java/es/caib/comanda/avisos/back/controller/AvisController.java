package es.caib.comanda.avisos.back.controller;

import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("avisController")
@RequestMapping(BaseConfig.API_PATH + "/avisos")
@Tag(name = "19. Avis", description = "Servei de gestió d'avisos'")
public class AvisController extends BaseMutableResourceController<Avis, Long> {

}
