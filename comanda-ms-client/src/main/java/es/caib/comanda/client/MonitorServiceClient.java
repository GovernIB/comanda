package es.caib.comanda.client;

import es.caib.comanda.client.model.monitor.Monitor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


/**
 * Client pel servei de monitor.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "monitor", url = "${es.caib.comanda.client.base.url}/monitors")
public interface MonitorServiceClient {

	@PostMapping
	ResponseEntity<EntityModel<Monitor>> create(
			@RequestBody final Monitor monitor,
			@RequestHeader("Authorization") final String authorizationHeader);

}
