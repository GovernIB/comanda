package es.caib.comanda.acl.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceConfigTest {

    @Test
    void getBasenames_retornaElsBasenamesDeMissatgesDelModulAcl() {
        // Verifica que la configuració de missatges ACL publica els basenames esperats.
        MessageSourceConfig config = new MessageSourceConfig();

        String[] basenames = ReflectionTestUtils.invokeMethod(config, "getBasenames");

        assertThat(basenames).containsExactly("comanda.acl-messages", "comanda.client-messages", "comanda-messages");
    }
}
