package es.caib.comanda.acl.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("aclEntryController")
@RequestMapping(BaseConfig.API_PATH + "/acl/entries")
@Tag(name = "27. ACL Entries", description = "Gestió de regles ACL")
public class AclEntryController extends BaseMutableResourceController<AclEntry, Long> {

    @Autowired
    private AclEntryService aclEntryService;

    @Operation(summary = "Crea múltiples entrades ACL en una sola crida")
    @PostMapping("/bulk")
    public List<AclEntry> createBulk(@RequestBody @Validated List<AclEntry> entries) {
        return aclEntryService.createAll(entries);
    }

    @Operation(summary = "Actualitza múltiples entrades ACL en una sola crida")
    @PutMapping("/bulk")
    public List<AclEntry> updateBulk(@RequestBody @Validated List<AclEntry> entries) {
        return aclEntryService.updateAll(entries);
    }

    @Operation(summary = "Elimina múltiples entrades ACL en una sola crida")
    @DeleteMapping("/bulk")
    public void deleteBulk(@RequestBody List<Long> ids) {
        aclEntryService.deleteAll(ids);
    }
}
