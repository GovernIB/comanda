package es.caib.comanda.permisos.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.permisos.logic.intf.model.Objecte;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

class PermisosControllersTest {

    @Test
    void objecteController_exposaRecursIRequestMappingEsperats() {
        // Verifica que el controller d'objectes queda lligat al recurs i al path REST del mòdul.
        ObjecteController controller = new ObjecteController();

        RequestMapping requestMapping = ObjecteController.class.getAnnotation(RequestMapping.class);
        RestController restController = ObjecteController.class.getAnnotation(RestController.class);
        Class<?> resourceClass = controller.getResourceClass();

        assertThat(restController.value()).isEqualTo("objecteController");
        assertThat(requestMapping.value()).containsExactly(BaseConfig.API_PATH + "/objectes");
        assertThat(resourceClass).isEqualTo(Objecte.class);
    }

    @Test
    void permisController_exposaRecursIRequestMappingEsperats() {
        // Verifica que el controller de permisos queda lligat al recurs i al path REST del mòdul.
        PermisController controller = new PermisController();

        RequestMapping requestMapping = PermisController.class.getAnnotation(RequestMapping.class);
        RestController restController = PermisController.class.getAnnotation(RestController.class);
        Class<?> resourceClass = controller.getResourceClass();

        assertThat(restController.value()).isEqualTo("permisController");
        assertThat(requestMapping.value()).containsExactly(BaseConfig.API_PATH + "/permisos");
        assertThat(resourceClass).isEqualTo(Permis.class);
    }

    @Test
    void controllers_generenRelBaseAPartirDelNomDelRecurs() {
        // Comprova que el link índex heretat usa el nom decapitalitzat del recurs corresponent.
        ObjecteController objecteController = new ObjecteController();
        PermisController permisController = new PermisController();

        Link objecteLink = ReflectionTestUtils.invokeMethod(objecteController, "getIndexLink");
        Link permisLink = ReflectionTestUtils.invokeMethod(permisController, "getIndexLink");

        assertThat(objecteLink.getRel().value()).isEqualTo("objecte");
        assertThat(permisLink.getRel().value()).isEqualTo("permis");
    }
}
