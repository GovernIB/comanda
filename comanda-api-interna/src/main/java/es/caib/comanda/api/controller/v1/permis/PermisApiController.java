package es.caib.comanda.api.controller.v1.permis;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.PermisServiceClient;
import es.caib.comanda.model.v1.permis.Permis;
import es.caib.comanda.ms.back.controller.BaseController;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
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
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
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

@RestController
@RequestMapping("/v1/permisos")
@Tag(name = "APP → COMANDA / Permisos",
        description = "Contracte per a la gestió CRUD de permisos a Comanda (informació dels permisos configurats a les aplicacions). " +
        "Les peticions rebudes per aquest servei es processaran asíncronament, de manera que en cap cas es rebrà una resposta amb el resultat de l'operació com a resposta de les peticions.")
@RequiredArgsConstructor
public class PermisApiController extends BaseController {

    private final JmsTemplate jmsTemplate;
    private final PermisServiceClient permisServiceClient;
    private final EntornAppServiceClient entornAppServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @PostMapping
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
//        if (permis.getAppCodi() == null || permis.getEntornCodi() == null) {
//            return ResponseEntity.badRequest().body("Cal informar appCodi i entornCodi al cos de la petició");
//        }
//        // Resol entornAppId per app/entorn
//        String entornAppFilter = "(entorn.codi:'" + permis.getEntornCodi().replace("'", "\\'") + "' and app.codi:'" + permis.getAppCodi().replace("'", "\\'") + "')";
//        PagedModel<EntityModel<es.caib.comanda.client.model.EntornApp>> entornApps = entornAppServiceClient.find(
//                null, entornAppFilter, null, null, "UNPAGED", null, httpAuthorizationHeaderHelper.getAuthorizationHeader());
//        Long entornAppId = entornApps.getContent().stream()
//                .map(EntityModel::getContent)
//                .filter(Objects::nonNull)
//                .map(es.caib.comanda.client.model.EntornApp::getId)
//                .findFirst().orElse(null);
//        if (entornAppId == null) return ResponseEntity.notFound().build();
//
//        // Comprova existència per entornAppId i objecte.identificador (identificador del path)
//        String filter = "(entornAppId:" + entornAppId + " and objecte.identificador:'" + identificador.replace("'", "\\'") + "')";
//        PagedModel<EntityModel<es.caib.comanda.client.model.permis.Permis>> page = permisServiceClient.find(
//                "", filter, null, null, "UNPAGED", null, httpAuthorizationHeaderHelper.getAuthorizationHeader());
//        boolean existeix = page.getContent().stream().findAny().isPresent();
//        if (!existeix) return ResponseEntity.notFound().build();
//        jmsTemplate.convertAndSend(CUA_PERMISOS, permis);
//        return ResponseEntity.ok("Missatge de modificació enviat a " + CUA_PERMISOS);
    }

    @PostMapping("/multiple")
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
//        if (permisos == null || permisos.isEmpty()) {
//            return ResponseEntity.badRequest().body("Llista buida");
//        }
//
//        // Construir filtre per una sola consulta: per cada (app,entorn) + (usuari.codi o grup) + objecte (tipus, identificador)
//        // 1) Obtenir entornAppId per tots els parells app/entorn
//        var grups = permisos.stream()
//                .filter(p -> p.getAppCodi() != null && p.getEntornCodi() != null && p.getObjecte() != null && p.getObjecte().getTipus() != null && p.getObjecte().getIdentificador() != null && (p.getUsuari() != null && p.getUsuari().getCodi() != null || p.getGrup() != null))
//                .collect(Collectors.groupingBy(p -> p.getAppCodi() + "|" + p.getEntornCodi()));
//
//        String entornAppFilter = grups.keySet().stream().map(k -> {
//            String[] parts = k.split("\\|");
//            String app = parts[0]; String ent = parts[1];
//            return "(entorn.codi:'" + ent.replace("'", "\\'") + "' and app.codi:'" + app.replace("'", "\\'") + "')";
//        }).collect(Collectors.joining(" or "));
//
//        PagedModel<EntityModel<es.caib.comanda.client.model.EntornApp>> entornApps = entornAppServiceClient.find(
//                null, entornAppFilter, null, null, "UNPAGED", null, httpAuthorizationHeaderHelper.getAuthorizationHeader());
//
//        var entornMap = entornApps.getContent().stream().map(EntityModel::getContent).filter(Objects::nonNull)
//                .collect(Collectors.toMap(ea -> ea.getApp().getNom() + "|" + ea.getEntorn().getNom(), es.caib.comanda.client.model.EntornApp::getId, (a,b)->a));
//
//        // 2) Construir OR de conjunts per (entornAppId) i (usuari.codi/grup) i objecte
//        String filter = permisos.stream().map(p -> {
//            Long entornAppId = entornMap.get(p.getAppCodi() + "|" + p.getEntornCodi());
//            if (entornAppId == null || p.getObjecte() == null || p.getObjecte().getTipus() == null || p.getObjecte().getIdentificador() == null) return null;
//            String subject = p.getUsuari() != null && p.getUsuari().getCodi() != null
//                    ? "usuari.codi:'" + p.getUsuari().getCodi().replace("'", "\\'") + "'"
//                    : (p.getGrup() != null ? "grup:'" + p.getGrup().replace("'", "\\'") + "'" : null);
//            if (subject == null) return null;
//            String obj = "objecte.tipus:'" + p.getObjecte().getTipus().replace("'", "\\'") + "' and objecte.identificador:'" + p.getObjecte().getIdentificador().replace("'", "\\'") + "'";
//            return "(entornAppId:" + entornAppId + " and " + subject + " and " + obj + ")";
//        }).filter(Objects::nonNull).distinct().collect(Collectors.joining(" or "));
//
//        if (filter == null || filter.isEmpty()) {
//            return ResponseEntity.status(404).body("No s'ha trobat cap permís a modificar");
//        }
//
//        PagedModel<EntityModel<es.caib.comanda.client.model.permis.Permis>> page = permisServiceClient.find(
//                "", filter, null, null, "UNPAGED", null, httpAuthorizationHeaderHelper.getAuthorizationHeader());
//
//        var existents = page.getContent().stream().map(EntityModel::getContent).filter(Objects::nonNull)
//                .map(x -> {
//                    String subject = x.getUsuari() != null && x.getUsuari().getCodi() != null ? x.getUsuari().getCodi() : (x.getGrup() != null ? x.getGrup() : "");
//                    return subject + "|" + (x.getObjecte()!=null?x.getObjecte().getTipus():"") + "|" + (x.getObjecte()!=null?x.getObjecte().getIdentificador():"");
//                }).collect(Collectors.toSet());
//
//        int enviats = 0; int ignorats = 0;
//        for (Permis p : permisos) {
//            String subject = p.getUsuari() != null && p.getUsuari().getCodi() != null ? p.getUsuari().getCodi() : (p.getGrup() != null ? p.getGrup() : "");
//            String key = subject + "|" + (p.getObjecte()!=null?p.getObjecte().getTipus():"") + "|" + (p.getObjecte()!=null?p.getObjecte().getIdentificador():"");
//            if (existents.contains(key)) {
//                jmsTemplate.convertAndSend(CUA_PERMISOS, p);
//                enviats++;
//            } else {
//                ignorats++;
//            }
//        }
//        if (enviats == 0) return ResponseEntity.status(404).body("No s'ha trobat cap permís a modificar");
//        return ResponseEntity.ok(enviats + " permisos modificats enviats; " + ignorats + " ignorats per no existir");
    }

    @GetMapping("/{identificador}")
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
//        try {
//            EntityModel<es.caib.comanda.client.model.permis.Permis> entity = permisServiceClient.getOne(id, null, httpAuthorizationHeaderHelper.getAuthorizationHeader());
//            if (entity == null || entity.getContent() == null) return ResponseEntity.notFound().build();
//            return ResponseEntity.ok(entity.getContent());
//        } catch (FeignException.NotFound nf) {
//            return ResponseEntity.notFound().build();
//        }
    }

//    @GetMapping
//    @Operation(operationId = "obtenirLlistatPermisos", summary = "Obté un llistat de permisos")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Llistat obtingut", content = @Content(
//                    schema = @Schema(implementation = Permis[].class),
//                    examples = @ExampleObject(name = "ExempleLlistatPermisos",
//                            value = "[{\n  \"id\": 55,\n  \"usuari\": { \"codi\": \"usr1234\" },\n  \"permisos\": [\"LECTURA\"],\n  \"objecte\": { \"tipus\": \"EXPEDIENT\", \"identificador\": \"EXP-12345\" }\n}]"))),
//            @ApiResponse(responseCode = "401", description = "No autenticat"),
//            @ApiResponse(responseCode = "403", description = "Prohibit"),
//            @ApiResponse(responseCode = "500", description = "Error intern")
//    })
//    public ResponseEntity<PagedModel<EntityModel<Permis>>> obtenirLlistatPermisos(
//            @Parameter(name="quickFilter", description = "Filtre ràpid") @RequestParam(value = "quickFilter", required = false, defaultValue = "") String quickFilter,
//            @Parameter(name="filter", description = "Filtre avançat en format JSON o expressió del MS") @RequestParam(value = "filter", required = false, defaultValue = "{}") String filter,
//            @Parameter(name="namedQueries", description = "Consultes predefinides") @RequestParam(value = "namedQueries", required = false) String[] namedQueries,
//            @Parameter(name="perspectives", description = "Perspectives de camp") @RequestParam(value = "perspectives", required = false) String[] perspectives,
//            @Parameter(name="page", description = "Número de pàgina") @RequestParam(value = "page", required = false, defaultValue = "0") String page,
//            @Parameter(name="size", description = "Mida de pàgina") @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
//        throw new NotImplementedException();
////        PagedModel<EntityModel<es.caib.comanda.client.model.permis.Permis>> result = permisServiceClient.find(quickFilter, filter, namedQueries, perspectives, page, size, httpAuthorizationHeaderHelper.getAuthorizationHeader());
////        List<es.caib.comanda.client.model.permis.Permis> list = result.getContent().stream().map(EntityModel::getContent).filter(Objects::nonNull).collect(Collectors.toList());
////        return ResponseEntity.ok(list);
//    }

    @DeleteMapping
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
//        if (permisos == null || permisos.isEmpty()) return ResponseEntity.badRequest().body("Llista buida");
//        int enviats = 0;
//        for (Permis p : permisos) {
//            // Indica a la capçalera del missatge que es tracta d'una operació de borrat
//            jmsTemplate.convertAndSend(CUA_PERMISOS, p, message -> {
//                message.setStringProperty("operation", "DELETE");
//                return message;
//            });
//            enviats++;
//        }
//        return ResponseEntity.ok(enviats + " peticions d'eliminació enviades a " + CUA_PERMISOS);
    }

    @Override
    protected Link getIndexLink() {
        return null;
    }

}
