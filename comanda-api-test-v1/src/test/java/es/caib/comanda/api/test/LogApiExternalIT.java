package es.caib.comanda.api.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.api.v1.monitoring.ComandaAppLogsApi;
import es.caib.comanda.model.v1.monitoring.FitxerContingut;
import es.caib.comanda.model.v1.monitoring.FitxerInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LogApiExternalIT {

    private static ComandaAppLogsApi api;
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
        api = ApiClientFactory.logApi();
    }

    @Test
    void llistarFitxers_ok() throws Exception {
        List<FitxerInfo> files = api.llistarFitxers();
        System.out.println(objectMapper.writeValueAsString(files));
        assertThat(files).isNotNull();
    }

    @Test
    void obtenirFitxer_ok() throws Exception {
        FitxerContingut file = api.getFitxerByNom("server.log");

        Files.write(Paths.get("/home/siona/tmp/logs.log"), file.getContingut());
        System.out.println(objectMapper.writeValueAsString(file));
        assertThat(file).isNotNull();
    }

    @Test
    void obtenirFilesFitxer_ok() throws Exception {
        List<String> files = api.llegitUltimesLinies("server.log", 50L);
        System.out.println(objectMapper.writeValueAsString(files));
        assertThat(files).isNotNull();
    }
}
