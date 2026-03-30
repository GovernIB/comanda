package es.caib.comanda.acl.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AclHalFormsConfigTest {

    @Test
    void getControllerPackages_retornaElPaquetDelControllerAcl() {
        // Comprova que la configuració HAL-FORMS apunta al paquet del controller ACL.
        AclHalFormsConfig config = new AclHalFormsConfig();

        String[] packages = ReflectionTestUtils.invokeMethod(config, "getControllerPackages");

        assertThat(packages).containsExactly("es.caib.comanda.acl.back.controller");
    }
}
