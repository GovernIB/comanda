package es.caib.comanda.usuaris.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.logic.intf.service.UsuariService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
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

    @GetMapping("/internal/by-codi/{codi}")
    @Operation(summary = "Consulta interna d'un usuari pel seu codi")
    @PreAuthorize("hasAnyAuthority(T(es.caib.comanda.base.config.BaseConfig).ROLE_WEBSERVICE, T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
    public ResponseEntity<EntityModel<Usuari>> getOneByCodiInternal(
            @PathVariable
            @Parameter(description = "Codi de l'usuari")
            String codi) throws ResourceNotFoundException {
        Usuari usuari = ((UsuariService) readonlyResourceService).findOneInternalByCodi(codi);
        return ResponseEntity.ok(EntityModel.of(usuari));
    }
}
