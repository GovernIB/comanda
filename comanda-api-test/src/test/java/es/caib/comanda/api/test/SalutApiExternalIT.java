package es.caib.comanda.api.test;

import es.caib.comanda.api.v1.salut.ComandaAppSalutApi;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SalutApiExternalIT {

    private static ComandaAppSalutApi api;

    @BeforeAll
    static void setup() {
        System.setProperty("javax.net.ssl.trustStore", "/home/siona/Feina/Server/webapps/config/keystores/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "******");
        System.setProperty(ClientProps2.PROP_BASE, "https://dev.caib.es/notibapi/interna");

        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps2.PROP_BASE + "=<baseUrl> to run these tests");
        api = ApiClientFactory.salutApi();
    }

    @Test
    void salut_ok() throws Exception {
        SalutInfo salut = api.salut();
        assertThat(salut).isNotNull();
    }

    @Test
    void salutInfo_ok() throws Exception {
        AppInfo info = api.salutInfo();
        assertThat(info).isNotNull();
    }
}
