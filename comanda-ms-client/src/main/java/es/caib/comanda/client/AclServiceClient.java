package es.caib.comanda.client;

import es.caib.comanda.client.model.acl.AclCheckRequest;
import es.caib.comanda.client.model.acl.AclCheckResponse;
import es.caib.comanda.client.model.acl.AclEntry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Client pel servei de configuració.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "app", url = "${es.caib.comanda.client.base.url}/acl")
public interface AclServiceClient {
    // CRUD
    @PostMapping("/entries")
    ResponseEntity<EntityModel<AclEntry>> create(
            @RequestBody final AclEntry aclEntry,
            @RequestHeader("Authorization") final String authorizationHeader);

    @PutMapping("/entries/{id}")
    ResponseEntity<EntityModel<AclEntry>> update(
            @PathVariable("id") final Long id,
            @RequestBody final AclEntry aclEntry,
            @RequestHeader("Authorization") final String authorizationHeader);

    @DeleteMapping("/entries/{id}")
    ResponseEntity<Void> delete(
            @PathVariable("id") final Long id,
            @RequestHeader("Authorization") final String authorizationHeader);

    @PostMapping("/entries/bulk")
    ResponseEntity<EntityModel<AclEntry>> createBulk(
            @RequestBody final AclEntry aclEntry,
            @RequestHeader("Authorization") final String authorizationHeader);

    @PutMapping("/entries/bulk")
    CollectionModel<EntityModel<AclEntry>> updateBulk(
            @RequestBody final AclEntry aclEntry,
            @RequestHeader("Authorization") final String authorizationHeader);

    @DeleteMapping("/entries/bulk")
    ResponseEntity<Void> deleteBulk(
            @RequestBody final List<Long> ids,
            @RequestHeader("Authorization") final String authorizationHeader);

    // Consulta
	@GetMapping(value = "/{id}")
	EntityModel<AclEntry> getOne(
			@PathVariable("id") final Long id,
			@RequestParam("perspectives") final String[] perspectives,
			@RequestHeader("Authorization") final String authorizationHeader);

	@GetMapping
	PagedModel<EntityModel<AclEntry>> find(
			@RequestParam("quickFilter") final String quickFilter,
			@RequestParam("filter") final String filter,
			@RequestParam("namedQueries") final String[] namedQueries,
			@RequestParam("perspectives") final String[] perspectives,
			@RequestParam("page") final String page,
			@RequestParam("size") final Integer size,
			@RequestHeader("Authorization") final String authorizationHeader);

    // Validar permisos
    @PostMapping("/check")
    ResponseEntity<EntityModel<AclCheckResponse>> programar(
            @RequestBody final AclCheckRequest request,
            @RequestHeader("Authorization") final String authorizationHeader);

}
