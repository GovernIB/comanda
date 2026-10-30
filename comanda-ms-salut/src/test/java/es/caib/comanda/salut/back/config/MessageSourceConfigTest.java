package es.caib.comanda.salut.back.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceConfigTest {

    @Test
    void getBasenames_retornaElsBasenamesDeMissatgesDelModulSalut() {
        MessageSourceConfig config = new MessageSourceConfig();

        String[] basenames = ReflectionTestUtils.invokeMethod(config, "getBasenames");

        assertThat(basenames).containsExactly("comanda-messages", "comanda.salut-messages", "comanda.client-messages");
    }
}
