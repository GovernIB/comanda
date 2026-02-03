package es.caib.comanda.api.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.api.monitoring.ComandaAppSalutApi;
import es.caib.comanda.model.monitoring.AppInfo;
import es.caib.comanda.model.monitoring.SalutInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SalutApiExternalIT {

    private static ComandaAppSalutApi api;
    private static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        System.out.println(objectMapper.writeValueAsString(salut));
        assertThat(salut).isNotNull();
    }

    @Test
    void salutInfo_ok() throws Exception {
        AppInfo info = api.salutInfo();
        System.out.println(objectMapper.writeValueAsString(info));
        assertThat(info).isNotNull();
    }
}
