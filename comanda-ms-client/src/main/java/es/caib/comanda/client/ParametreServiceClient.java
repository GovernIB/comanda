package es.caib.comanda.client;

import es.caib.comanda.client.model.Parametre;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client pel servei de configuració - Parametres.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "parametre", url = "${es.caib.comanda.client.base.url}/parametres")
public interface ParametreServiceClient {

//	@GetMapping(value = "/{id}")
//	EntityModel<Parametre> getOne(
//			@PathVariable("id") final Long id,
//			@RequestParam("perspectives") final String[] perspectives,
//			@RequestHeader("Authorization") final String authorizationHeader);

	@GetMapping
	PagedModel<EntityModel<Parametre>> find(
			@RequestParam("quickFilter") final String quickFilter,
			@RequestParam("filter") final String filter,
			@RequestParam("namedQueries") final String[] namedQueries,
			@RequestParam("perspectives") final String[] perspectives,
			@RequestParam("page") final String page,
			@RequestParam("size") final Integer size,
			@RequestHeader("Authorization") final String authorizationHeader);

}
