package es.caib.comanda.acl.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.acl.logic.intf.dto.AclCheckRequest;
import es.caib.comanda.acl.logic.intf.dto.AclCheckResponse;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("aclController")
@RequestMapping(BaseConfig.API_PATH + "/acl")
@Tag(name = "ACL", description = "Operacions ACL")
@RequiredArgsConstructor
public class AclController {

    private final AclEntryService aclEntryService;

    @Operation(summary = "Comprova si un subjecte té permís sobre un recurs")
    @PostMapping("/check")
    public AclCheckResponse check(@RequestBody @Validated AclCheckRequest request) {
        boolean allowed = aclEntryService.checkPermission(
                request.getUser(),
                request.getRoles(),
                request.getResourceType(),
                request.getResourceId(),
                request.getAction());
        return new AclCheckResponse(allowed);
    }
}
