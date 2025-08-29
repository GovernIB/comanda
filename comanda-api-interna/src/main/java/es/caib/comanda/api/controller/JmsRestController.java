package es.caib.comanda.api.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseController;
import es.caib.comanda.ms.broker.model.Avis;
import es.caib.comanda.ms.broker.model.Permis;
import es.caib.comanda.ms.broker.model.Tasca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static es.caib.comanda.ms.broker.model.Cues.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(BaseConfig.API_PATH + "/jms")
@Tag(name = "JMS", description = "Servei d'enviament de missatges a les cues JMS")
@RequiredArgsConstructor
public class JmsRestController extends BaseController {

	private final JmsTemplate jmsTemplate;

	@GetMapping
	@Operation(summary = "Llista d'opcions disponibles")
	public ResponseEntity<CollectionModel<?>> index() {
		List<Link> indexLinks = new ArrayList<>();
		indexLinks.add(linkTo(methodOn(getClass()).sendToTasquesQueue(null)).withRel("tasques"));
		indexLinks.add(linkTo(methodOn(getClass()).sendToAvisosQueue(null)).withRel("avisos"));
		indexLinks.add(linkTo(methodOn(getClass()).sendToPermisosQueue(null)).withRel("permisos"));
		CollectionModel<?> resources = CollectionModel.of(
				Collections.emptySet(),
				indexLinks.toArray(Link[]::new));
		return ResponseEntity.ok(resources);
	}

	@PostMapping("/tasques")
	@Operation(summary = "Afegeix un missatge a la cua de tasques")
	public ResponseEntity<String> sendToTasquesQueue(@RequestBody Tasca tasca) {
		jmsTemplate.convertAndSend(CUA_TASQUES, tasca);
		return ResponseEntity.ok("Missatge enviat a " + CUA_TASQUES);
	}

	@PostMapping("/avisos")
	@Operation(summary = "Afegeix un missatge a la cua d'avisos")
	public ResponseEntity<String> sendToAvisosQueue(@RequestBody Avis avis) {
		jmsTemplate.convertAndSend(CUA_AVISOS, avis);
		return ResponseEntity.ok("Missatge enviat a " + CUA_AVISOS);
	}

	@PostMapping("/permisos")
	@Operation(summary = "Afegeix un missatge a la cua de permisos")
	public ResponseEntity<String> sendToPermisosQueue(@RequestBody Permis permis) {
		jmsTemplate.convertAndSend(CUA_PERMISOS, permis);
		return ResponseEntity.ok("Missatge enviat a " + CUA_PERMISOS);
	}

	@Override
	protected Link getIndexLink() {
		return linkTo(methodOn(getClass()).index()).withRel("jms");
	}

}
