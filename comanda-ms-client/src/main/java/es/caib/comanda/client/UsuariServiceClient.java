package es.caib.comanda.client;

import es.caib.comanda.client.model.EntornApp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client pel servei de configuració.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "usuari", url = "${es.caib.comanda.client.base.url}/usuaris")
public interface UsuariServiceClient {

	@GetMapping(value = "/{id}")
	EntityModel<EntornApp> getOne(
			@PathVariable("id") final Long id,
			@RequestParam("perspectives") final String[] perspectives,
			@RequestHeader("Authorization") final String authorizationHeader);

	@GetMapping
	PagedModel<EntityModel<EntornApp>> find(
			@RequestParam("quickFilter") final String quickFilter,
			@RequestParam("filter") final String filter,
			@RequestParam("namedQueries") final String[] namedQueries,
			@RequestParam("perspectives") final String[] perspectives,
			@RequestParam("page") final String page,
			@RequestParam("size") final Integer size,
			@RequestHeader("Authorization") final String authorizationHeader);

}
