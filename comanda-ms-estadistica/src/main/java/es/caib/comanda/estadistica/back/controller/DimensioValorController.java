package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de dimensions estadístiques.
 *
 * @author Límit Tecnologies
 */
@RestController("dimensioValorController")
@RequestMapping(BaseConfig.API_PATH + "/dimensioValors")
@Tag(name = "DimensioValor", description = "Servei de consulta de dimensio-Valors")
public class DimensioValorController extends BaseMutableResourceController<DimensioValor, Long> {

}
