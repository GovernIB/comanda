package es.caib.comanda.api.controller.v1.tasca;

import es.caib.comanda.api.helper.ApiClientHelper;
import es.caib.comanda.api.util.ApiMapper;
import es.caib.comanda.model.v1.tasca.Tasca;
import es.caib.comanda.model.v1.tasca.TascaPage;
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
import java.util.Set;
import java.util.stream.Collectors;

import static es.caib.comanda.base.config.Cues.CUA_TASQUES;

@RestController
@RequestMapping("/v1/tasques")
@Tag(name = "APP → COMANDA / Tasques",
        description = "Contracte per a la gestió CRUD de tasques a Comanda. " +
        "Les peticions rebudes per aquest servei es processaran asíncronament, de manera que en cap cas es rebrà una resposta amb el resultat de l'operació com a resposta de les peticions.")
@RequiredArgsConstructor
public class TascaApiController extends BaseController {

    private final JmsTemplate jmsTemplate;
    private final ApiClientHelper apiClientHelper;
    private final ApiMapper apiMapper;

    @PostMapping
    @Operation(
            operationId = "crearTasca",
            summary = "Creació s'una tasca",
            description = "Afegeix un missatge d'alta de tasca a una cua de events per a que es crei aquesta de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> crearTasca(
            @Parameter(name = "tasca", description = "Dades de la tasca a publicar", required = true)
            @RequestBody(description = "Dades de la tasca a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = Tasca.class)))
            @org.springframework.web.bind.annotation.RequestBody Tasca tasca) {
        jmsTemplate.convertAndSend(CUA_TASQUES, tasca);
        return ResponseEntity.ok("Missatge enviat a " + CUA_TASQUES);
    }

    @PutMapping("/{identificador}")
    @Operation(
            operationId = "modificarTasca",
            summary = "Modificació una tasca",
            description = "Es comprova si la tasca existeix, i en cas afirmatiu, s'afegeix un missatge de modificació de tasca a una cua de events per a que es modifiqui aquesta de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatge acceptat", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> modificarTasca(
            @Parameter(name = "identificador", description = "Identificador de la tasca", required = true) @PathVariable String identificador,
            @Parameter(name = "tasca", description = "Dades de la tasca a modificar", required = true)
            @RequestBody(description = "Dades de la tasca a modificar", required = true, content = @Content(schema = @Schema(implementation = Tasca.class)))
            @org.springframework.web.bind.annotation.RequestBody Tasca tasca) {

        if (tasca.getIdentificador() == null)
            return ResponseEntity.badRequest().body("Cal informar identificador al cos de la petició");
        if (!identificador.equalsIgnoreCase(tasca.getIdentificador()))
            return ResponseEntity.badRequest().body("L'identificador de la petició ha de coincidir amb el del cos de la petició");
        if (tasca.getAppCodi() == null || tasca.getEntornCodi() == null)
            return ResponseEntity.badRequest().body("Cal informar appCodi i entornCodi al cos de la petició");
        
        Boolean existTasca = apiClientHelper.existTasca(tasca.getIdentificador(), tasca.getAppCodi(), tasca.getEntornCodi());
        if (!existTasca) {
            return ResponseEntity.notFound().build();
        }
        jmsTemplate.convertAndSend(CUA_TASQUES, tasca);
        return ResponseEntity.ok("Missatge de modificació enviat a " + CUA_TASQUES);
    }

    @PostMapping("/multiple")
    @Operation(
            operationId = "crearMultiplesTasques",
            summary = "Creació de múltiples tasques",
            description = "Afegeix múltiples missatges d'alta de tasques a una cua de events per a que es creïn aquestes de forma asíncrona a Comanda."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> crearMultiplesTasques(
            @Parameter(name = "tasques", description = "Llista de tasques a publicar", required = true)
            @RequestBody(description = "Llista de tasques a publicar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Tasca> tasques) {
        for (Tasca tasca : tasques) {
            jmsTemplate.convertAndSend(CUA_TASQUES, tasca);
        }
        return ResponseEntity.ok(tasques.size() + " missatges enviats a " + CUA_TASQUES);
    }

    @PutMapping("/multiple")
    @Operation(
            operationId = "modificarMultiplesTasques",
            summary = "Modificació de múltiples tasques",
            description = "Es comprova si les tasques existeixen, i en cas afirmatiu, s'afegeixen múltiples missatges de modificació de tasques a una cua de events per a que es modifiquin aquestes de forma asíncrona a Comanda. Les tasques no existents s'ignoren."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Missatges acceptats", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Petició incorrecta"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<String> modificarMultiplesTasques(
            @Parameter(name = "tasques", description = "Llista de tasques a modificar", required = true)
            @RequestBody(description = "Llista de tasques a modificar", required = true,
                    content = @Content(schema = @Schema(implementation = List.class)))
            @org.springframework.web.bind.annotation.RequestBody List<Tasca> tasques) {

        // Validacions
        if (tasques == null || tasques.isEmpty()) {
            return ResponseEntity.badRequest().body("Llista buida");
        }

        // Validar que totes les tasques tenen el mateix entornCodi i appCodi
        String entornCodi = tasques.get(0).getEntornCodi();
        String appCodi = tasques.get(0).getAppCodi();
        Set<String> identificadors = new HashSet<>();

        for (Tasca t : tasques) {
            if (t.getIdentificador() == null || t.getEntornCodi() == null || t.getAppCodi() == null) {
                return ResponseEntity.badRequest().body("Totes les tasques han de tenir identficador, entornCodi i appCodi informats");
            }
            if (!t.getEntornCodi().equals(entornCodi) || !t.getAppCodi().equals(appCodi)) {
                return ResponseEntity.badRequest().body("Totes les tasques han de tenir el mateix entornCodi i appCodi");
            }
            identificadors.add(t.getIdentificador());
        }

        List<es.caib.comanda.client.model.Tasca> tasquesExistents = apiClientHelper.getTasques(identificadors, appCodi, entornCodi);
        if (tasquesExistents == null || tasquesExistents.isEmpty()) {
            return ResponseEntity.status(404).body("No s'ha trobat cap tasca a modificar");
        }

        var identificadorsExistents = tasquesExistents.stream().map(t -> t.getIdentificador()).collect(Collectors.toList());
        int enviats = 0;
        int ignorats = 0;
        for (Tasca t : tasques) {
            if (identificadorsExistents.contains(t.getIdentificador())) {
                jmsTemplate.convertAndSend(CUA_TASQUES, t);
                enviats++;
            } else {
                ignorats++;
            }
        }

        if (enviats == 0) {
            return ResponseEntity.status(404).body("No s'ha trobat cap tasca a modificar");
        }
        return ResponseEntity.ok(enviats + " tasques modificades enviades; " + ignorats + " ignorades per no existir");
    }

    @GetMapping("/{identificador}")
    @Operation(
            operationId = "consultarTasca",
            summary = "Consulta d'una tasca",
            description = "Obté les dades d'una tasca identificada pel seu identificador, codi d'aplicació i codi d'entorn."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasca trobada", content = @Content(
                    schema = @Schema(implementation = Tasca.class),
                    examples = @ExampleObject(name = "ExempleTasca",
                            value = "{\n  \"id\": 123,\n  \"identificador\": \"TSK-2025-0001\",\n  \"tipus\": \"ACCIO\",\n  \"nom\": \"Revisar expedient\",\n  \"descripcio\": \"Validar dades de l\u2019expedient\",\n  \"estat\": \"PENDENT\",\n  \"estatDescripcio\": \"Pendent de revisi\u00F3\",\n  \"prioritat\": \"ALTA\",\n  \"dataInici\": \"2025-12-12T08:00:00\",\n  \"dataFi\": null,\n  \"dataCaducitat\": \"2025-12-31T23:59:59\",\n  \"url\": \"https://dev.caib.es/app/tasques/TSK-2025-0001\",\n  \"responsable\": \"usr1234\",\n  \"grup\": \"SUPORT\"\n}"))) ,
            @ApiResponse(responseCode = "404", description = "Tasca no trobada"),
            @ApiResponse(responseCode = "401", description = "No autenticat"),
            @ApiResponse(responseCode = "403", description = "Prohibit"),
            @ApiResponse(responseCode = "500", description = "Error intern")
    })
    public ResponseEntity<Tasca> consultarTasca(
            @Parameter(name = "identificador", description = "Identificador de la tasca", required = true) @PathVariable String identificador,
            @Parameter(name = "appCodi", description = "Codi de l'aplicació", required = true) @RequestParam String appCodi,
            @Parameter(name = "entornCodi", description = "Codi de l'entorn", required = true) @RequestParam String entornCodi) {

        es.caib.comanda.client.model.Tasca tasca = apiClientHelper.getTasca(identificador, appCodi, entornCodi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No s'ha trobat cap tasca amb l'identificador: " + identificador));
        return ResponseEntity.ok(apiMapper.convert(tasca));
    }

    @GetMapping
    @Operation(
            operationId = "obtenirLlistatTasques",
            summary = "Consulta de tasques",
            description = "Obté un llistat paginat de tasques amb possibilitat d'aplicar filtres ràpids, filtres avançats, consultes predefinides i perspectives."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Llistat obtingut", content = @Content(
                    schema = @Schema(implementation = TascaPage.class),
                    examples = @ExampleObject(
                            name = "ExempleLlistatTasques",
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
    public ResponseEntity<TascaPage> obtenirLlistatTasques(
            @Parameter(name="quickFilter", description = "Filtre ràpid") @RequestParam(value = "quickFilter", required = false, defaultValue = "") String quickFilter,
            @Parameter(name="filter", description = "Filtre avançat en format JSON o expressió del MS") @RequestParam(value = "filter", required = false, defaultValue = "{}") String filter,
            @Parameter(name="namedQueries", description = "Consultes predefinides") @RequestParam(value = "namedQueries", required = false) String[] namedQueries,
            @Parameter(name="perspectives", description = "Perspectives de camp") @RequestParam(value = "perspectives", required = false) String[] perspectives,
            @Parameter(name="page", description = "Número de pàgina") @RequestParam(value = "page", required = false, defaultValue = "0") String page,
            @Parameter(name="size", description = "Mida de pàgina") @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        PagedModel<EntityModel<es.caib.comanda.client.model.Tasca>> result = apiClientHelper.getTasques(
                quickFilter,
                filter,
                namedQueries,
                perspectives,
                page,
                size);
        // Map a model API (comanda-api-models)
        List<Tasca> mappedContent = result.getContent().stream()
                .map(em -> apiMapper.convert(em.getContent()))
                .collect(Collectors.toList());
        TascaPage tascaPage = new TascaPage(
                mappedContent,
                TascaPage.PageMetadata.builder()
                        .number(result.getMetadata().getNumber())
                        .size(result.getMetadata().getSize())
                        .totalElements(result.getMetadata().getTotalElements())
                        .totalPages(result.getMetadata().getTotalPages())
                        .build(),
                result.getLinks().stream()
                        .map(l -> TascaPage.Link.builder().rel(l.getRel().value()).href(l.getHref()).build())
                        .collect(Collectors.toUnmodifiableList()));
        return ResponseEntity.ok(tascaPage);
    }

    @Override
    protected Link getIndexLink() {
        return null;
    }
}
