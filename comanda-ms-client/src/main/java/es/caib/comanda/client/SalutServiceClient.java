package es.caib.comanda.client;

import es.caib.comanda.client.model.Salut;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;

/**
 * Client pel servei de salut.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "salut", url = "${es.caib.comanda.client.base.url}/saluts")
public interface SalutServiceClient {

	@GetMapping
	PagedModel<EntityModel<Salut>> find(
			@RequestParam("quickFilter") final String quickFilter,
			@RequestParam("filter") final String filter,
			@RequestParam("namedQueries") final String[] namedQueries,
			@RequestParam("perspectives") final String[] perspectives,
			@RequestParam("page") final String page,
			@RequestParam("size") final Integer size,
			@RequestParam("sort") final String[] sort,
			@RequestHeader("Authorization") final String authorizationHeader);

}
