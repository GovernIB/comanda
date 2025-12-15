package es.caib.comanda.api.test;

import es.caib.comanda.api.v1.estadistica.ComandaAppEstadstiquesApi;
import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EstadisticaApiExternalIT {

    private static ComandaAppEstadstiquesApi api;

    @BeforeAll
    static void setup() {
        System.setProperty("javax.net.ssl.trustStore", "/home/siona/Feina/Server/webapps/config/keystores/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "******");
        System.setProperty(ClientProps2.PROP_BASE, "https://dev.caib.es/notibapi/interna");

        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps2.PROP_BASE + "=<baseUrl> to run these tests");
        api = ApiClientFactory.estadisticaApi();
    }

    @Test
    void estadistiques_ok() throws Exception {
        RegistresEstadistics res = api.estadistiques();
        assertThat(res).isNotNull();
    }

    @Test
    void estadistiques_info() throws Exception {
        EstadistiquesInfo res = api.estadistiquesInfo();
        assertThat(res).isNotNull();
    }
}
