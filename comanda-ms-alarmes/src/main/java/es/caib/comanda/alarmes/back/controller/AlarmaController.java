package es.caib.comanda.alarmes.back.controller;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController
@RequestMapping(BaseConfig.API_PATH + "/alarmes")
@Tag(name = "24. Alarma", description = "Servei de gestió d'alarmes")
public class AlarmaController extends BaseMutableResourceController<Alarma, Long> {

}
