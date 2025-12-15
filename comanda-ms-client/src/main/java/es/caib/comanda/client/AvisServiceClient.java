package es.caib.comanda.client;

import es.caib.comanda.client.model.Avis;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "avis", url = "${es.caib.comanda.client.base.url}/avisos")
public interface AvisServiceClient {

    @GetMapping(value = "/{id}")
    EntityModel<Avis> getOne(
            @PathVariable("id") final Long id,
            @RequestParam(value = "perspectives", required = false) final String[] perspectives,
            @RequestHeader("Authorization") final String authorizationHeader);

    @GetMapping
    PagedModel<EntityModel<Avis>> find(
            @RequestParam(value = "quickFilter", required = false) final String quickFilter,
            @RequestParam(value = "filter", required = false) final String filter,
            @RequestParam(value = "namedQueries", required = false) final String[] namedQueries,
            @RequestParam(value = "perspectives", required = false) final String[] perspectives,
            @RequestParam("page") final String page,
            @RequestParam("size") final Integer size,
            @RequestHeader("Authorization") final String authorizationHeader);
}
