package es.caib.comanda.api.test;

import es.caib.comanda.api.v1.log.ComandaAppLogsApi;
import es.caib.comanda.model.v1.log.FitxerInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LogApiExternalIT {

    private static ComandaAppLogsApi api;

    @BeforeAll
    static void setup() {
        System.setProperty("javax.net.ssl.trustStore", "/home/siona/Feina/Server/webapps/config/keystores/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "******");
        System.setProperty(ClientProps2.PROP_BASE, "https://dev.caib.es/notibapi/interna");
        System.setProperty(ClientProps2.PROP_USER, "$not_com");
        System.setProperty(ClientProps2.PROP_PWD, "******");

        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps2.PROP_BASE + "=<baseUrl> to run these tests");
        api = ApiClientFactory.logApi();
    }

    @Test
    void llistarFitxers_ok() throws Exception {
        List<FitxerInfo> files = api.llistarFitxers();
        assertThat(files).isNotNull();
    }
}
