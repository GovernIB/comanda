package es.caib.comanda.api.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.api.monitoring.ComandaAppEstadistiquesApi;
import es.caib.comanda.model.monitoring.EstadistiquesInfo;
import es.caib.comanda.model.monitoring.RegistresEstadistics;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EstadisticaApiExternalIT {

    private static ComandaAppEstadistiquesApi api;
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
        System.setProperty(ClientProps2.PROP_USER, "$com_not");
        System.setProperty(ClientProps2.PROP_PWD, "******");

        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps2.PROP_BASE + "=<baseUrl> to run these tests");
        api = ApiClientFactory.estadisticaApi();
    }

    @Test
    void estadistiques_ok() throws Exception {
        RegistresEstadistics res = api.estadistiques();
        System.out.println(objectMapper.writeValueAsString(res));
        assertThat(res).isNotNull();
    }

    @Test
    void estadistiques_info() throws Exception {
        EstadistiquesInfo res = api.estadistiquesInfo();
        System.out.println(objectMapper.writeValueAsString(res));
        assertThat(res).isNotNull();
    }
}
