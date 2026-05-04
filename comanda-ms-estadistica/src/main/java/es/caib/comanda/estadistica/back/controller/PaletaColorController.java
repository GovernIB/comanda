package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("paletaColorController")
@RequestMapping(BaseConfig.API_PATH + "/paletaColor")
@Tag(name = "?. Paleta color", description = "Servei de gestio de colors de paleta")
public class PaletaColorController extends BaseMutableResourceController<PaletaColor, Long> {
}
