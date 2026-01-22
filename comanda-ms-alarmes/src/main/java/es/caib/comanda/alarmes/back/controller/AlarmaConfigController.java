package es.caib.comanda.alarmes.back.controller;

import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de gestió de configuracions d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController
@RequestMapping(BaseConfig.API_PATH + "/alarmaConfigs")
@Tag(name = "25. AlarmaConfig", description = "Servei de gestió de configuracions d'alarmes")
public class AlarmaConfigController extends BaseMutableResourceController<AlarmaConfig, Long> {

}
