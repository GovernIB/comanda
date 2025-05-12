package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.estadistica.logic.service.EstadisticaSchedulerService;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.model.Resource;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.groups.Default;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController("fetController")
@RequestMapping(BaseConfig.API_PATH + "/fets")
@Tag(name = "Fet", description = "Servei de consulta de fets")
public class FetController extends BaseReadonlyResourceController<Fet, Long> {

    @Autowired
    private EstadisticaSchedulerService schedulerService;

    @PostMapping("/programar")
    public ResponseEntity<Void> create(
            @RequestBody
            @Validated({Resource.OnCreate.class, Default.class})
            final EntornApp entornApp) {
        log.info("Rebuda petició d'actualització de procés de salut per entornApp: {}", entornApp.getId());
        schedulerService.programarTasca(entornApp);
        return ResponseEntity.ok().build();
    }

    //    GET /api/fets/estadistiques/periode/dimensions
    //    ?entornAppId=1
    //            &dataInici=2024-01-01
    //            &dataFi=2024-03-31
    //            &nivellAgrupacio=MES
    //    &dimensions[departament]=RRHH,Informàtica
    //    &dimensions[tipus]=INTERN

    /**
     * Retorna una llista d'objectes de tipus Fet que corresponen a les estadístiques filtrades per període de temps i dimensions específiques.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació al qual s'han d'aplicar les estadístiques.
     * @param dataInici Data d'inici del període per obtenir les estadístiques (en format ISO).
     * @param dataFi Data de finalització del període per obtenir les estadístiques (en format ISO).
     * @param allParams Paràmetres addicionals que inclouen les dimensions que s'han de processar.
     *                  Les dimensions s'han de proporcionar en un dels següents formats:
     *                  - dimensions.ENT=21&dimensions.PRC=215043
     *                  - dimensions[ENT]=21&dimensions[PRC]=215043 (claudàtors codificats com %5B i %5D).
     * @return Una entitat de resposta HTTP {@code ResponseEntity} que conté una llista de dades de tipus {@code Fet}.
     */
    @GetMapping("/estadistiques/periode/dimensions")
    public ResponseEntity<List<Fet>> getEstadistiquesPeriodeAmbDimensions(
            @RequestParam Long entornAppId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
//            @RequestParam(required = false) Map<String, List<String>> dimensions) {
            @RequestParam MultiValueMap<String, String> allParams) {
//            @RequestParam NivellAgrupacio nivellAgrupacio) {

        // Reconstruir les dimensions en un Map<String, List<String>>
        Map<String, List<String>> dimensions = allParams.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("dimensions.") || entry.getKey().startsWith("dimensions[")) // Filtrar els noms de dimensions
                .collect(Collectors.toMap(
                        entry -> extractDimensionKey(entry.getKey()), // Extreure la clau: ENT o PRC
                        Map.Entry::getValue // Ja és una llista de valors gràcies a MultiValueMap
                ));

        return ResponseEntity.ok(
                ((FetService)getReadonlyResourceService()).getEstadistiquesPeriodeAmbDimensions(
                        entornAppId,
                        dataInici,
                        dataFi,
                        dimensions
//                        nivellAgrupacio
                )
        );
    }

    /**
     * Extreu la clau de dimensió d'una cadena completa que segueix un format específic.
     *
     * @param fullKey La cadena completa que conté la informació de la dimensió, normalment amb prefix "dimensions." o "dimensions[".
     * @return La clau de dimensió processada sense el prefix ni caràcters addicionals, o la cadena original si no correspon als formats predefinits.
     */
    private String extractDimensionKey(String fullKey) {
        if (fullKey.startsWith("dimensions.")) {
            return fullKey.substring("dimensions.".length()); // Retorna després del punt
        } else if (fullKey.startsWith("dimensions[")) {
            return fullKey.substring("dimensions[".length(), fullKey.length() - 1); // Retorna dins dels claudàtors
        }
        return fullKey; // Per si apareix un cas inesperat (no hauria de passar)
    }



    //    GET /api/fets/estadistiques/periode?entornAppId=1&dataInici=2024-01-01&dataFi=2024-03-31

    /**
     * Retorna una llista d'objectes de tipus Fet que corresponen a les estadístiques filtrades per un període de temps especificat.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació al qual s'aplica la consulta d'estadístiques.
     * @param dataInici Data d'inici del període per obtenir les estadístiques (en format ISO).
     * @param dataFi Data de finalització del període per obtenir les estadístiques (en format ISO).
     * @return Una entitat de resposta HTTP {@code ResponseEntity} que conté una llista de dades de tipus {@code Fet}.
     */
    @GetMapping("/estadistiques/periode")
    public ResponseEntity<List<Fet>> getEstadistiquesPeriode(
            @RequestParam Long entornAppId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi) {
//            @RequestParam NivellAgrupacio nivellAgrupacio) {

        return ResponseEntity.ok(
                ((FetService)getReadonlyResourceService()).getEstadistiquesPeriode(
                        entornAppId,
                        dataInici,
                        dataFi
//                        nivellAgrupacio
                )
        );
    }


    //    GET /api/fets/estadistiques/migrar?entornAppId=1

    /**
     * Obtenció de dades estadístiques d'ahir per a un entorn d'aplicació específic.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació per al qual es realitza la migració de dades.
     * @return Una entitat de resposta HTTP {@code ResponseEntity} amb el resultat de l'operació. Retorna "OK" si la migració s'ha
     *         completat correctament o un missatge d'error si ha fallat.
     */
    @GetMapping("/estadistiques/migrar")
    public ResponseEntity<String> getMigrarDades(@RequestParam Long entornAppId) {
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        try {
            ((FetService)getReadonlyResourceService()).migrarDades(entornAppId); //, data);
            return ResponseEntity.ok("OK");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("ERROR: " + ex.getMessage());
        }
    }


}
