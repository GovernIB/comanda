package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.db.DbTaulaActivitat;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("dbTaulaActivitatController")
@RequestMapping(BaseConfig.API_PATH + "/db-taules-activitat")
@Tag(name = "31. DB Taules Activitat", description = "Activitat de taules de la base de dades")
public class DbTaulaActivitatController extends BaseReadonlyResourceController<DbTaulaActivitat, String> {
}
