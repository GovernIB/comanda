package es.caib.comanda.acl.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.acl.AclCheckRequest;
import es.caib.comanda.client.model.acl.AclCheckResponse;
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

import java.util.List;

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
        boolean allowed;
        if (request.getAction() != null) {
            // Compatibilitat enrere: una sola acció
            allowed = aclEntryService.checkPermission(
                    request.getUser(),
                    request.getRoles(),
                    request.getResourceType(),
                    request.getResourceId(),
                    request.getAction());
        } else {
            List<es.caib.comanda.client.model.acl.AclAction> actions = request.getActions();
            AclCheckRequest.Mode mode = request.getMode() == null ? AclCheckRequest.Mode.ANY : request.getMode();
            if (actions == null || actions.isEmpty()) {
                allowed = false;
            } else if (mode == AclCheckRequest.Mode.ALL) {
                allowed = aclEntryService.checkPermissionsAll(
                        request.getUser(), request.getRoles(), request.getResourceType(), request.getResourceId(), actions);
            } else {
                allowed = aclEntryService.checkPermissionsAny(
                        request.getUser(), request.getRoles(), request.getResourceType(), request.getResourceId(), actions);
            }
        }
        return new AclCheckResponse(allowed);
    }
}
