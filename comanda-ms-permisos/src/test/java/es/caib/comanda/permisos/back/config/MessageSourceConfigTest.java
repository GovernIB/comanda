package es.caib.comanda.permisos.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceConfigTest {

    @Test
    void getBasenames_retornaElsBasenamesDeMissatgesDelModulPermisos() {
        // Verifica que la configuració de missatges publica els basenames esperats del mòdul.
        MessageSourceConfig config = new MessageSourceConfig();

        String[] basenames = ReflectionTestUtils.invokeMethod(config, "getBasenames");

        assertThat(basenames).containsExactly("comanda.permisos-messages", "comanda.client-messages", "comanda-messages");
    }
}
