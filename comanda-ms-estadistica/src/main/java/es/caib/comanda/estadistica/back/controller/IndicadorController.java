package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de indicadors estadístics.
 *
 * @author Límit Tecnologies
 */
@RestController("indicadorController")
@RequestMapping(BaseConfig.API_PATH + "/indicadors")
@Tag(name = "10. Indicador", description = "Servei de consulta de indicadors")
public class IndicadorController extends BaseMutableResourceController<Indicador, Long> {

}
