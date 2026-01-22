package es.caib.comanda.acl.back.controller;

import es.caib.comanda.acl.logic.intf.model.ResourceType;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController("aclEntryController")
@RequestMapping(BaseConfig.API_PATH + "/aclEntries")
@Tag(name = "26. ACL Entries", description = "Gestió de regles ACL")
public class AclEntryController extends BaseMutableResourceController<AclEntry, String> {

	private final AclEntryService aclEntryService;

	@GetMapping("/anyPermissionGranted")
	public ResponseEntity<Boolean> anyPermissionGranted(
			@RequestParam ResourceType resourceType,
			@RequestParam Serializable resourceId,
			@RequestParam List<PermissionEnum> permissions) {
		boolean granted = aclEntryService.anyPermissionGranted(
				resourceType,
				resourceId,
				permissions);
		return ResponseEntity.ok(granted);
	}

	@GetMapping("/findIdsWithAnyPermission")
	public ResponseEntity<Set<Serializable>> findIdsWithAnyPermission(
			@RequestParam ResourceType resourceType,
			@RequestParam List<PermissionEnum> permissions) {
		Set<Serializable> ids = aclEntryService.findIdsWithAnyPermission(
				resourceType,
				permissions);
		return ResponseEntity.ok(ids);
	}

}
