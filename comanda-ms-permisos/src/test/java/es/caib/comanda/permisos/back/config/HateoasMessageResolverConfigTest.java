package es.caib.comanda.permisos.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class HateoasMessageResolverConfigTest {

    @Test
    void getBasename_retornaElBasenameRestDelModulPermisos() {
        // Comprova que el resolver HATEOAS usa el basename REST específic del mòdul.
        HateoasMessageResolverConfig config = new HateoasMessageResolverConfig();

        String basename = ReflectionTestUtils.invokeMethod(config, "getBasename");

        assertThat(basename).isEqualTo("comanda.permisos-rest-messages");
    }
}
