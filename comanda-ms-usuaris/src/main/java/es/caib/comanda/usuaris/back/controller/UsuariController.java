package es.caib.comanda.usuaris.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.logic.intf.service.UsuariService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController("usuariController")
@RequestMapping(BaseConfig.API_PATH + "/usuaris")
@Tag(name = "19. Usuari", description = "Servei de gestió d'usuaris")
public class UsuariController extends BaseMutableResourceController<Usuari, Long> {

    // Mètode per obtenir un usuari per codi amb la mínima sobrecàrrega possible (per utilitzar al client feign)
    @GetMapping(value = "/byCodi/{codi}")
    @Operation(summary = "Consulta la informació d'un usuari per codi")
    @PreAuthorize("this.isPublic() or hasPermission(#resourceId, this.getResourceClass().getName(), this.getOperation('GET_ONE'))")
    public ResponseEntity<Usuari> getByCodi(
            @PathVariable
            @Parameter(description = "Codi de l'usuari")
            final String codi) {
        log.debug("Obtenint usuari (codi={})", codi);
        Usuari usuari = ((UsuariService) getReadonlyResourceService()).getOneByCodi(codi);
        if (usuari == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuari);
    }

}
