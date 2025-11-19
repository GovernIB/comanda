package es.caib.comanda.client;

import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Client pel servei de configuració.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "aclEntries", url = "${es.caib.comanda.client.base.url}/aclEntries")
public interface AclServiceClient {

	@GetMapping("/anyPermissionGranted")
	ResponseEntity<Boolean> anyPermissionGranted(
			@RequestParam ResourceType resourceType,
			@RequestParam Serializable resourceId,
			@RequestParam List<PermissionEnum> permissions, // Ha de ser una llista de PermissionEnum
			@RequestHeader("Authorization") final String authorizationHeader);

	@GetMapping("/findIdsWithAnyPermission")
	ResponseEntity<Set<Serializable>> findIdsWithAnyPermission(
			@RequestParam ResourceType resourceType,
			@RequestParam List<PermissionEnum> permissions, // Ha de ser una llista de PermissionEnum
			@RequestHeader("Authorization") final String authorizationHeader);

}
