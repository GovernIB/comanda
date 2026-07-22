package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.db.DbIndex;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("dbIndexController")
@RequestMapping(BaseConfig.API_PATH + "/db-indexos")
@Tag(name = "35. DB Índexos", description = "Índexos de la base de dades")
public class DbIndexController extends BaseMutableResourceController<DbIndex, String> {
}
