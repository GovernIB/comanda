package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de paleta.
 *
 * @author Límit Tecnologies
 */
@RestController("paletaController")
@RequestMapping(BaseConfig.API_PATH + "/paleta")
@Tag(name = "?. Paleta", description = "Servei de consulta de paleta")
public class PaletaController extends BaseMutableResourceController<Paleta, Long> {

}
