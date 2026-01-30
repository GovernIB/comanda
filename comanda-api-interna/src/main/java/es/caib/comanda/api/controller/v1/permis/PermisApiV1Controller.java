package es.caib.comanda.api.controller.v1.permis;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.PermisServiceClient;
import es.caib.comanda.model.v1.permis.Permis;
import es.caib.comanda.ms.back.controller.BaseController;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static es.caib.comanda.base.config.Cues.CUA_PERMISOS;
import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.BASIC_SECURITY_SCHEME;
import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.SECURITY_NAME;

@RestController
@RequestMapping("/permisos/v1")
@Tag(name = "APP → COMANDA / Permisos",
        description = "Contracte per a la gestió CRUD de permisos a Comanda (informació dels permisos configurats a les aplicacions). " +
        "Les peticions rebudes per aquest servei es processaran asíncronament, de manera que en cap cas es rebrà una resposta amb el resultat de l'operació com a resposta de les peticions.")
@RequiredArgsConstructor
@SecurityScheme(type = SecuritySchemeType.HTTP, name = SECURITY_NAME, scheme = BASIC_SECURITY_SCHEME)
@PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_WEBSERVICE)")
public class PermisApiV1Controller extends BaseController {

    private final JmsTemplate jmsTemplate;
    private final PermisServiceClient permisServiceClient;
    private final EntornAppServiceClient entornAppServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @PostMapping
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(
            operationId = "crearPermis",
            summary = "Creació d'un permís",
            description = "Afegeix un missatge d'alta de permís a una cua de events per a que es crei aquest de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> crearPermis(
            @Parameter(name = "permis", description = "Dades de la sol·licitud de permisos a publicar", required = true)
            @RequestBody(description = "Dades de la sol·licitud de permisos a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = Permis.class)))
            @org.springframework.web.bind.annotation.RequestBody Permis permis) {
        jmsTemplate.convertAndSend(CUA_PERMISOS, permis);
        return ResponseEntity.ok("Missatge enviat a " + CUA_PERMISOS);
    }

    @PutMapping("/{identificador}")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(
            operationId = "modificarPermis",
            summary = "Modificació d'un permís existent",
            description = "Es comprova si el permís existeix, i en cas afirmatiu, s'afegeix un missatge de modificació de permís a una cua de events per a que es modifiqui aquest de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Permís no trobat"),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> modificarPermis(
            @Parameter(name = "identificador", description = "Identificador funcional (objecte.identificador)", required = true) @PathVariable String identificador,
            @Parameter(name = "permis", description = "Dades de la sol·licitud a modificar", required = true)
            @RequestBody(description = "Dades de la sol·licitud a modificar", required = true,
                    content = @Content(schema = @Schema(implementation = Permis.class)))
            @org.springframework.web.bind.annotation.RequestBody Permis permis) {

        throw new NotImplementedException();
    }

    @PostMapping("/multiple")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(
            operationId = "crearMultiplesPermisos",
            summary = "Creació de múltiples permisos",
            description = "Afegeix múltiples missatges d'alta de permisos a una cua de events per a que es creïn aquests de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> crearMultiplesPermisos(
            @Parameter(name = "permisos", description = "Llista de permisos a publicar", required = true)
            @RequestBody(description = "Llista de permisos a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Permis> permisos) {
        for (Permis p : permisos) {
            jmsTemplate.convertAndSend(CUA_PERMISOS, p);
        }
        return ResponseEntity.ok(permisos.size() + " missatges enviats a " + CUA_PERMISOS);
    }

    @PutMapping("/multiple")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(
            operationId = "modificarMultiplesPermisos",
            summary = "Modificació de múltiples permisos",
            description = "Es comprova si els permisos existeixen, i en cas afirmatiu, s'afegeixen múltiples missatges de modificació de permisos a una cua de events per a que es modifiquin aquests de forma asíncrona a Comanda. Els permisos no existents s'ignoren."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Cap permís trobat"),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> modificarMultiplesPermisos(
            @Parameter(name = "permisos", description = "Llista de permisos a modificar", required = true)
            @RequestBody(description = "Llista de permisos a modificar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Permis> permisos) {
        throw new NotImplementedException();
    }

    @GetMapping("/{identificador}")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(
            operationId = "consultarPermis",
            summary = "Consulta d'un permís",
            description = "Obté les dades d'un permís identificat pel seu identificador, codi d'aplicació i codi d'entorn."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permís trobat", content = @Content(
                    schema = @Schema(implementation = Permis.class),
                    examples = @ExampleObject(name = "ExemplePermis",
                            value = "{\n  \"id\": 55,\n  \"usuari\": { \"codi\": \"usr1234\" },\n  \"grup\": null,\n  \"permisos\": [\"LECTURA\", \"ESCRIPTURA\"],\n  \"objecte\": { \"tipus\": \"EXPEDIENT\", \"identificador\": \"EXP-12345\" }\n}"))),
            @ApiResponse(responseCode = "404", description = "Permís no trobat"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<Permis> consultarPermis(
            @Parameter(name = "identificador", description = "Identificador del permís", required = true) @PathVariable String identificador,
            @Parameter(name = "appCodi", description = "Codi de l'aplicació", required = true) @RequestParam String appCodi,
            @Parameter(name = "entornCodi", description = "Codi de l'entorn", required = true) @RequestParam String entornCodi) {
        throw new NotImplementedException();
    }

    @DeleteMapping
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(
            operationId = "eliminarPermisos",
            summary = "Eliminació de permisos",
            description = "Afegeix múltiples missatges d'eliminació de permisos a una cua de events per a que s'eliminin aquests de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges d'eliminació acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> eliminarPermisos(
            @RequestBody(description = "Llista de permisos a eliminar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Permis> permisos) {
        throw new NotImplementedException();
    }

    @Override
    protected Link getIndexLink() {
        return null;
    }

}
