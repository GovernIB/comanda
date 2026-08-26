package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorFormulaTerme;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de gestió dels termes de fórmula d'indicadors.
 *
 * @author Límit Tecnologies
 */
@RestController("indicadorFormulaTermeController")
@RequestMapping(BaseConfig.API_PATH + "/indicadorFormulaTermes")
@Tag(name = "10. Indicador", description = "Servei de gestió dels termes de fórmula d'indicadors")
public class IndicadorFormulaTermeController extends BaseMutableResourceController<IndicadorFormulaTerme, Long> {

}
