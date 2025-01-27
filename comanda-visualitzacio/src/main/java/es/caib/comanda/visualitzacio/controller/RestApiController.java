package es.caib.comanda.visualitzacio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RestApiController {

	@Value("${es.caib.comanda.visualitzacio.app.service.url}")
	private String appServiceUrl;
	@Value("${es.caib.comanda.visualitzacio.salut.service.url}")
	private String salutServiceUrl;

	@GetMapping
	public ResponseEntity<CollectionModel<?>> index(
			@RequestHeader HttpHeaders headers) throws MalformedURLException, URISyntaxException {
		List<Link> indexLinks = Arrays.asList(
				Link.of(toForwardedHeadersUrl(appServiceUrl, headers)).withRel("app"),
				Link.of(toForwardedHeadersUrl(salutServiceUrl, headers)).withRel("salut"));
		CollectionModel<?> resources = CollectionModel.of(
				Collections.emptySet(),
				indexLinks.toArray(Link[]::new));
		return ResponseEntity.ok(resources);
	}

	private String toForwardedHeadersUrl(
			String uri,
			HttpHeaders headers) throws URISyntaxException, MalformedURLException {
		URL url = new URI(uri).toURL();
		String xForwardedProto = headers.getFirst("x-forwarded-proto");
		String xForwardedHost = headers.getFirst("x-forwarded-host");
		if (xForwardedHost != null && xForwardedHost.contains(":")) {
			xForwardedHost = xForwardedHost.split(":")[0];
		}
		String xForwardedPort = headers.getFirst("x-forwarded-port");
		return new URL(
				xForwardedProto != null ? xForwardedProto : url.getProtocol(),
				xForwardedHost != null ? xForwardedHost : url.getHost(),
				xForwardedPort != null ? Integer.parseInt(xForwardedPort) : url.getPort(),
				url.getFile()).toString();
	}

}
