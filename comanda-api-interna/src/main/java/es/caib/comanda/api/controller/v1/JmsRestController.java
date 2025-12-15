package es.caib.comanda.api.controller.v1;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.permis.Permis;
import es.caib.comanda.model.v1.tasca.Tasca;
import es.caib.comanda.ms.back.controller.BaseController;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static es.caib.comanda.base.config.Cues.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Deprecated
@RestController
@RequestMapping(BaseConfig.API_PATH + "/v1/jms")
@Tag(name = "APP → COMANDA / JMS", description = "Enviament de missatges a les cues JMS de COMANDA: tasques, avisos i permisos. Un missatge és una unitat d'informació publicada a una cua JMS perquè sigui processada de manera asíncrona.")
@RequiredArgsConstructor
public class JmsRestController extends BaseController {

    private final JmsTemplate jmsTemplate;

    @Hidden
    @GetMapping
    @Operation(
            summary = "Llista d'opcions disponibles",
            description = "Index d'enllaços a les operacions d'enviament de missatges JMS disponibles (tasques, avisos i permisos)."
    )
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
    @Operation(
            summary = "Publica una tasca a la cua de tasques",
            description = "Afegeix un missatge de tipus Tasca a la cua JMS 'CUA_TASQUES'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> sendToTasquesQueue(
            @RequestBody(description = "Dades de la tasca a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = Tasca.class)))
            @org.springframework.web.bind.annotation.RequestBody Tasca tasca) {
        jmsTemplate.convertAndSend(CUA_TASQUES, tasca);
        return ResponseEntity.ok("Missatge enviat a " + CUA_TASQUES);
    }

    @PostMapping("/avisos")
    @Operation(
            summary = "Publica un avís a la cua d'avisos",
            description = "Afegeix un missatge d'avís a la cua JMS 'CUA_AVISOS'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> sendToAvisosQueue(
            @RequestBody(description = "Dades de l'avís a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = Avis.class)))
            @org.springframework.web.bind.annotation.RequestBody Avis avis) {
        jmsTemplate.convertAndSend(CUA_AVISOS, avis);
        return ResponseEntity.ok("Missatge enviat a " + CUA_AVISOS);
    }

    @PostMapping("/permisos")
    @Operation(
            summary = "Publica una sol·licitud de permisos a la cua de permisos",
            description = "Afegeix un missatge de permisos a la cua JMS 'CUA_PERMISOS'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> sendToPermisosQueue(
            @RequestBody(description = "Dades de la sol·licitud de permisos a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = Permis.class)))
            @org.springframework.web.bind.annotation.RequestBody Permis permis) {
        jmsTemplate.convertAndSend(CUA_PERMISOS, permis);
        return ResponseEntity.ok("Missatge enviat a " + CUA_PERMISOS);
    }

    @Override
    protected Link getIndexLink() {
        return linkTo(methodOn(getClass()).index()).withRel("jms");
    }

}
