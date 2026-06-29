package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.db.DbTaulaStorage;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("dbTaulaStorageController")
@RequestMapping(BaseConfig.API_PATH + "/db-taules-storage")
@Tag(name = "30. DB Taules Storage", description = "Emmagatzematge de taules de la base de dades")
public class DbTaulaStorageController extends BaseReadonlyResourceController<DbTaulaStorage, String> {
}
