package es.caib.comanda.api.controller.v1.avis;

import es.caib.comanda.api.helper.ApiClientHelper;
import es.caib.comanda.api.util.ApiMapper;
import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.avis.AvisPage;
import es.caib.comanda.ms.back.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static es.caib.comanda.base.config.Cues.CUA_AVISOS;

@RestController
@RequestMapping("/v1/avisos")
@Tag(name = "APP → COMANDA / Avisos", 
        description = "Contracte per a la gestió CRUD d'avisos a Comanda. " +
        "Les peticions rebudes per aquest servei es processaran asíncronament, de manera que en cap cas es rebrà una resposta amb el resultat de l'operació com a resposta de les peticions.")
@RequiredArgsConstructor
public class AvisApiController extends BaseController {

    private final JmsTemplate jmsTemplate;
    private final ApiClientHelper apiClientHelper;
    private final ApiMapper apiMapper;

    @PostMapping
    @Operation(
            operationId = "crearAvis",
            summary = "Creació d'un avís",
            description = "Afegeix un missatge d'alta d'avís a una cua de events per a que es crei aquest de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> crearAvis(
            @Parameter(name = "avis", description = "Dades de l'avís a publicar", required = true)
            @RequestBody(description = "Dades de l'avís a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = Avis.class)))
            @org.springframework.web.bind.annotation.RequestBody Avis avis) {
        jmsTemplate.convertAndSend(CUA_AVISOS, avis);
        return ResponseEntity.ok("Missatge enviat a " + CUA_AVISOS);
    }

    @PutMapping("/{identificador}")
    @Operation(
            operationId = "modificarAvis",
            summary = "Modificació d'un avís existent",
            description = "Es comprova si l'avís existeix, i en cas afirmatiu, s'afegeix un missatge de modificació d'avís a una cua de events per a que es modifiqui aquest de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Avís no trobat"),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> modificarAvis(
            @Parameter(name = "identificador", description = "Identificador de l'avís", required = true) @PathVariable String identificador,
            @Parameter(name = "avis", description = "Dades de l'avís a modificar", required = true)
            @RequestBody(description = "Dades de l'avís a modificar", required = true,
                    content = @Content(schema = @Schema(implementation = Avis.class)))
            @org.springframework.web.bind.annotation.RequestBody Avis avis) {

        if (avis.getIdentificador() == null)
            return ResponseEntity.badRequest().body("Cal informar identificador al cos de la petició");
        if (!identificador.equalsIgnoreCase(avis.getIdentificador()))
            return ResponseEntity.badRequest().body("L'identificador de la petició ha de coincidir amb el del cos de la petició");
        if (avis.getAppCodi() == null || avis.getEntornCodi() == null)
            return ResponseEntity.badRequest().body("Cal informar appCodi i entornCodi al cos de la petició");

        Boolean existAvis = existAvis(avis.getIdentificador(), avis.getAppCodi(), avis.getEntornCodi());
        if (!existAvis) return ResponseEntity.notFound().build();
        jmsTemplate.convertAndSend(CUA_AVISOS, avis);
        return ResponseEntity.ok("Missatge de modificació enviat a " + CUA_AVISOS);
    }

    @PostMapping("/multiple")
    @Operation(
            operationId = "crearMultiplesAvisos",
            summary = "Creació de múltiples avisos",
            description = "Afegeix múltiples missatges d'alta d'avisos a una cua de events per a que es creïn aquests de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> crearMultiplesAvisos(
            @Parameter(name = "avisos", description = "Llista d'avisos a publicar", required = true)
            @RequestBody(description = "Llista d'avisos a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Avis> avisos) {
        for (Avis avis : avisos) {
            jmsTemplate.convertAndSend(CUA_AVISOS, avis);
        }
        return ResponseEntity.ok(avisos.size() + " missatges enviats a " + CUA_AVISOS);
    }

    @PutMapping("/multiple")
    @Operation(
            operationId = "modificarMultiplesAvisos",
            summary = "Modificació de múltiples avisos",
            description = "Es comprova si els avisos existeixen, i en cas afirmatiu, s'afegeixen missatges de modificació d'avisos a una cua de events per a que es modifiquin aquests de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Cap avís trobat"),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> modificarMultiplesAvisos(
            @Parameter(name = "avisos", description = "Llista d'avisos a modificar", required = true)
            @RequestBody(description = "Llista d'avisos a modificar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Avis> avisos) {
        
        // Validacions
        if (avisos == null || avisos.isEmpty()) {
            return ResponseEntity.badRequest().body("Llista buida");
        }

        // Validar que tots els avisos tenen el mateix entornCodi i appCodi
        String entornCodi = avisos.get(0).getEntornCodi();
        String appCodi = avisos.get(0).getAppCodi();
        Set<String> identificadors = new HashSet<>();

        for (Avis a : avisos) {
            if (a.getIdentificador() == null || a.getEntornCodi() == null || a.getAppCodi() == null) {
                return ResponseEntity.badRequest().body("Tots els avisos han de tenir identificador, entornCodi i appCodi informats");
            }
            if (!a.getEntornCodi().equals(entornCodi) || !a.getAppCodi().equals(appCodi)) {
                return ResponseEntity.badRequest().body("Tots els avisos han de tenir el mateix entornCodi i appCodi");
            }
            identificadors.add(a.getIdentificador());
        }

        List<es.caib.comanda.client.model.Avis> avisosExistents = getAvisosByCodi(identificadors, appCodi, entornCodi);
        if (avisosExistents == null || avisosExistents.isEmpty()) {
            return ResponseEntity.status(404).body("No s'ha trobat cap avís a modificar");
        }

        var identificadorsExistents = avisosExistents.stream().map(a -> a.getIdentificador()).collect(Collectors.toList());
        int enviats = 0; 
        int ignorats = 0;
        for (Avis a : avisos) {
            if (identificadorsExistents.contains(a.getIdentificador())) {
                jmsTemplate.convertAndSend(CUA_AVISOS, a);
                enviats++;
            } else {
                ignorats++;
            }
        }
        if (enviats == 0) {
            return ResponseEntity.status(404).body("No s'ha trobat cap avís a modificar");
        }
        return ResponseEntity.ok(enviats + " avisos modificats enviats; " + ignorats + " ignorats per no existir");
    }

    @GetMapping("/{identificador}")
    @Operation(
            operationId = "consultarAvis",
            summary = "Consulta d'un avís",
            description = "Obté les dades d'un avís a partir del seu identificador, codi d'aplicació i codi d'entorn."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avís trobat", content = @Content(
                    schema = @Schema(implementation = Avis.class),
                    examples = @ExampleObject(name = "ExempleAvis",
                            value = "{\n  \"id\": 10,\n  \"identificador\": \"AV-2025-0001\",\n  \"tipus\": \"MANTENIMENT\",\n  \"nom\": \"Interrupci\u00F3 programada\",\n  \"descripcio\": \"Aturada de manteniment el diumenge a les 8:00\",\n  \"dataInici\": \"2025-12-13T08:00:00.000+00:00\",\n  \"dataFi\": \"2025-12-13T10:00:00.000+00:00\",\n  \"redireccio\": \"https://dev.caib.es/app/avis/AV-2025-0001\",\n  \"responsable\": \"usr1234\",\n  \"grup\": \"SUPORT\"\n}"))),
            @ApiResponse(responseCode = "404", description = "Avís no trobat"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<Avis> consultarAvis(
            @Parameter(name = "identificador", description = "Identificador de l'avís", required = true) @PathVariable String identificador,
            @Parameter(name = "appCodi", description = "Codi de l'aplicació", required = true) @RequestParam String appCodi,
            @Parameter(name = "entornCodi", description = "Codi de l'entorn", required = true) @RequestParam String entornCodi) {

        es.caib.comanda.client.model.Avis avis = getAvisByCodi(identificador, appCodi, entornCodi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No s'ha trobat cap avís amb l'identificador: " + identificador));
        return ResponseEntity.ok(apiMapper.convert(avis));
    }

    @GetMapping
    @Operation(
            operationId = "obtenirLlistatAvisos",
            summary = "Llistat d'avisos",
            description = "Obté un llistat paginat d'avisos amb la possibilitat d'aplicar filtres de cerca."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Llistat obtingut", content = @Content(
                    schema = @Schema(implementation = AvisPage.class),
                    examples = @ExampleObject(
                            name = "ExempleLlistatAvisos",
                            value = "{\n" +
                                    "  \"content\": [\n" +
                                    "    {\n" +
                                    "      \"id\": 10,\n" +
                                    "      \"identificador\": \"AV-2025-0001\",\n" +
                                    "      \"tipus\": \"MANTENIMENT\",\n" +
                                    "      \"nom\": \"Interrupció programada\"\n" +
                                    "    }\n" +
                                    "  ],\n" +
                                    "  \"page\": {\n" +
                                    "    \"number\": 0,\n" +
                                    "    \"size\": 20,\n" +
                                    "    \"totalElements\": 1,\n" +
                                    "    \"totalPages\": 1\n" +
                                    "  }\n" +
                                    "}"
                    )
                )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<AvisPage> obtenirLlistatAvisos(
            @Parameter(name="quickFilter", description = "Filtre ràpid") @RequestParam(value = "quickFilter", required = false, defaultValue = "") String quickFilter,
            @Parameter(name="filter", description = "Filtre avançat en format JSON o expressió del MS") @RequestParam(value = "filter", required = false, defaultValue = "{}") String filter,
            @Parameter(name="namedQueries", description = "Consultes predefinides") @RequestParam(value = "namedQueries", required = false) String[] namedQueries,
            @Parameter(name="perspectives", description = "Perspectives de camp") @RequestParam(value = "perspectives", required = false) String[] perspectives,
            @Parameter(name="page", description = "Número de pàgina") @RequestParam(value = "page", required = false, defaultValue = "0") String page,
            @Parameter(name="size", description = "Mida de pàgina") @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        PagedModel<EntityModel<es.caib.comanda.client.model.Avis>> result = apiClientHelper.getAvisos(
                quickFilter,
                filter,
                namedQueries,
                perspectives,
                page,
                size);
        // Map a model API (comanda-api-models)
        List<Avis> mappedContent = result.getContent().stream()
                .map(em -> apiMapper.convert(em.getContent()))
                .collect(Collectors.toList());
        AvisPage avisPage = new AvisPage(
                mappedContent,
                AvisPage.PageMetadata.builder()
                        .number(result.getMetadata().getNumber())
                        .size(result.getMetadata().getSize())
                        .totalElements(result.getMetadata().getTotalElements())
                        .totalPages(result.getMetadata().getTotalPages())
                        .build(),
                result.getLinks().stream()
                        .map(l -> AvisPage.Link.builder().rel(l.getRel().value()).href(l.getHref()).build())
                        .collect(Collectors.toUnmodifiableList()));
        return ResponseEntity.ok(avisPage);
    }

    @Override
    protected Link getIndexLink() {
        return null;
    }

    public Optional<es.caib.comanda.client.model.Avis> getAvisByCodi(String identificador, String appCodi, String entornCodi) {
        Long appId = apiClientHelper.getAppByCodi(appCodi).get().getId();
        Long entornId = apiClientHelper.getEntornByCodi(entornCodi).get().getId();
        return apiClientHelper.getAvis(identificador, appId, entornId);
    }

    private Boolean existAvis(String identificador, String appCodi, String entornCodi) {
        return getAvisByCodi(identificador, appCodi, entornCodi).isPresent();
    }

    private List<es.caib.comanda.client.model.Avis> getAvisosByCodi(Set<String> identificadors, String appCodi, String entornCodi) {
        Long appId = apiClientHelper.getAppByCodi(appCodi).get().getId();
        Long entornId = apiClientHelper.getEntornByCodi(entornCodi).get().getId();
        return apiClientHelper.getAvisos(identificadors, appId, entornId);
    }
}
