package es.caib.comanda.monitor.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.db.DbBloqueig;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("dbBloqueigController")
@RequestMapping(BaseConfig.API_PATH + "/db-bloqueigs")
@Tag(name = "34. DB Bloquejos", description = "Bloquejos de sessions a la base de dades")
public class DbBloqueigController extends BaseReadonlyResourceController<DbBloqueig, Long> {
}
