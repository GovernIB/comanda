package es.caib.comanda.visualitzacio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RestApiController {

	@Value("${es.caib.comanda.app.service.url}")
	private String appServiceUrl;
	@Value("${es.caib.comanda.salut.service.url}")
	private String salutServiceUrl;

	@GetMapping
	public ResponseEntity<CollectionModel<?>> index() {
		List<Link> indexLinks = Arrays.asList(
				Link.of(appServiceUrl).withRel("app"),
				Link.of(salutServiceUrl).withRel("salut"));
		CollectionModel<?> resources = CollectionModel.of(
				Collections.emptySet(),
				indexLinks.toArray(Link[]::new));
		return ResponseEntity.ok(resources);
	}

}
