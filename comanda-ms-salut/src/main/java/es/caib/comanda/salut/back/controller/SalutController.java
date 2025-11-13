package es.caib.comanda.salut.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.back.controller.BaseReadonlyResourceController;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.service.SalutSchedulerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.groups.Default;

/**
 * Servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RestController("salutController")
@RequestMapping(BaseConfig.API_PATH + "/saluts")
@Tag(name = "6. Salut", description = "Servei de consulta d'informació de salut")
public class SalutController extends BaseReadonlyResourceController<Salut, Long> {

    @Autowired
    private SalutSchedulerService schedulerService;

    @PostMapping ("/programar")
    @PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_ADMIN)")
    public ResponseEntity<Void> create(
            @RequestBody
            @Validated({Resource.OnCreate.class, Default.class})
            final EntornApp entornApp) {
        log.info("Rebuda petició d'actualització de procés de salut per entornApp: {}", entornApp.getId());
        schedulerService.programarTasca(entornApp);
        return ResponseEntity.ok().build();
    }

}
