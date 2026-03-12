package es.caib.comanda.permisos.back.config;

import es.caib.comanda.permisos.back.controller.PermisController;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermisosHalFormsConfigTest {

    @Test
    void getControllerPackages_retornaElPaquetDelControllerDePermisos() {
        // Comprova que la configuració HAL-FORMS escaneja el paquet correcte dels controllers del mòdul.
        PermisosHalFormsConfig config = new PermisosHalFormsConfig();

        String[] packages = config.getControllerPackages();

        assertThat(packages).containsExactly(PermisController.class.getPackageName());
    }
}
