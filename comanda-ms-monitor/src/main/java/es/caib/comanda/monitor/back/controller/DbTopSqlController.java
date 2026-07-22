package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.db.DbTopSql;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("dbTopSqlController")
@RequestMapping(BaseConfig.API_PATH + "/db-top-sql")
@Tag(name = "32. DB Top SQL", description = "Consultes SQL amb més cost a la base de dades")
public class DbTopSqlController extends BaseReadonlyResourceController<DbTopSql, String> {
}
