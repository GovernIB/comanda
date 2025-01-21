package es.caib.comanda.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class RestApiController {

	@Value("${server.port:8080}")
	private int serverPort;
	@Value("${es.caib.comanda.app.service.url}")
	private String appServiceUrl;
	@Value("${es.caib.comanda.salut.service.url}")
	private String salutServiceUrl;

	@GetMapping
	public ResponseEntity<CollectionModel<?>> index() throws MalformedURLException, URISyntaxException {
		List<Link> indexLinks = Arrays.asList(
				Link.of(toLocalHostPort(appServiceUrl)).withRel("app"),
				Link.of(toLocalHostPort(salutServiceUrl)).withRel("salut"));
		CollectionModel<?> resources = CollectionModel.of(
				Collections.emptySet(),
				indexLinks.toArray(Link[]::new));
		return ResponseEntity.ok(resources);
	}

	private String toLocalHostPort(String uri) throws URISyntaxException, MalformedURLException {
		URL url = new URI(uri).toURL();
		return new URI(
				url.getProtocol(),
				null,
				"localhost",
				serverPort,
				url.getFile(),
				url.getQuery(),
				null).toURL().toString();
	}

}
