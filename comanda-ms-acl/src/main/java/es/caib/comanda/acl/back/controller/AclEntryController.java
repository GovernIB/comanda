package es.caib.comanda.acl.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("aclEntryController")
@RequestMapping(BaseConfig.API_PATH + "/acl/entries")
@Tag(name = "ACL Entries", description = "Gestió de regles ACL")
public class AclEntryController extends BaseMutableResourceController<AclEntry, Long> {
}
