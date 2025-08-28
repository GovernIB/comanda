package es.caib.comanda.client;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.Monitor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Client pel servei de estadística.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "estadistica", url = "${es.caib.comanda.client.base.url}/fets")
public interface EstadisticaServiceClient {

	@PostMapping("/programar")
	ResponseEntity<EntityModel<Monitor>> programar(
			@RequestBody final EntornApp entornApp,
			@RequestHeader("Authorization") final String authorizationHeader);

}
