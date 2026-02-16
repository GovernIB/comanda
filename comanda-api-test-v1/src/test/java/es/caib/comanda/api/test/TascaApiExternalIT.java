package es.caib.comanda.api.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.api.management.AppComandaTasquesApi;
import es.caib.comanda.model.management.Prioritat;
import es.caib.comanda.model.management.Tasca;
import es.caib.comanda.model.management.TascaEstat;
import es.caib.comanda.model.management.TascaPage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TascaApiExternalIT {

    private static AppComandaTasquesApi api;
    private static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeAll
    static void setup() {
        System.setProperty("javax.net.ssl.trustStore", "/home/siona/Feina/Server/webapps/config/keystores/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "******");
        System.setProperty(ClientProps2.PROP_BASE, "http://localhost:8080/comandaapi/interna");
        System.setProperty(ClientProps2.PROP_USER, "com_ws");
        System.setProperty(ClientProps2.PROP_PWD, "******");

        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps2.PROP_BASE + "=<baseUrl> to run these tests");
        api = ApiClientFactory.tasquesApi();
    }

    @Test
    void obtenirLlistatTasques_ok() throws Exception {
        TascaPage res = api.obtenirLlistatTasques(null, null, null, null, null, 10);
        System.out.println(objectMapper.writeValueAsString(res));
        assertThat(res).isNotNull();
        assertThat(res.getContent()).isNotNull();
    }

    @Test
    void consultarTasca_ok() throws Exception {
        // First get a list to obtain a valid task id
        TascaPage page = api.obtenirLlistatTasques(null, null, null, null, null, 1);
        Assumptions.assumeTrue(page.getContent() != null && !page.getContent().isEmpty(),
                "No tasks available for testing");

        Tasca firstTasca = page.getContent().get(0);
        String identificador = firstTasca.getIdentificador();
        String appCodi = firstTasca.getAppCodi();
        String entornCodi = firstTasca.getEntornCodi();

        Tasca res = api.consultarTasca(identificador, appCodi, entornCodi);
        System.out.println(objectMapper.writeValueAsString(res));
        assertThat(res).isNotNull();
        assertThat(res.getIdentificador()).isEqualTo(identificador);
    }

    @Test
    void crearTasca_ok() throws Exception {
        Tasca tasca = new Tasca();
        tasca.setIdentificador("test-tasca-" + System.currentTimeMillis());
        tasca.setAppCodi("NOT");
        tasca.setEntornCodi("DEV");
        tasca.setNom("Tasca de prova");
        tasca.setDescripcio("Descripció de la tasca de prova");
        tasca.setTipus("PROVA");
        tasca.setEstat(TascaEstat.PENDENT);
        tasca.setEstatDescripcio("Pendent");
        tasca.setDataInici(OffsetDateTime.now());
        tasca.setDataCaducitat(OffsetDateTime.now().plusDays(10));
        tasca.setUsuarisAmbPermis(List.of("com_admin"));
        tasca.setPrioritat(Prioritat.ALTA);
        tasca.setNumeroExpedient("EXP-123456789");
        tasca.setRedireccio(new URL("http://localhost:8080/tasca/mock"));

        String res = api.crearTasca(tasca);
        System.out.println("Create response: " + res);
        assertThat(res).isNotNull();
    }

    @Test
    void crearMultiplesTasques_ok() throws Exception {
        Tasca tasca1 = new Tasca();
        tasca1.setIdentificador("test-tasca-batch-1-" + System.currentTimeMillis());
        tasca1.setAppCodi("NOT");
        tasca1.setEntornCodi("DEV");
        tasca1.setNom("Tasca de prova 1");
        tasca1.setDescripcio("Primera tasca del batch");
        tasca1.setTipus("PROVA");
        tasca1.setEstat(TascaEstat.PENDENT);
        tasca1.setEstatDescripcio("Pendent");
        tasca1.setDataInici(OffsetDateTime.now());
        tasca1.setDataCaducitat(OffsetDateTime.now().plusDays(10));
        tasca1.setUsuarisAmbPermis(List.of("com_admin"));
        tasca1.setPrioritat(Prioritat.ALTA);
        tasca1.setRedireccio(new URL("http://localhost:8080/tasca/mock"));

        Tasca tasca2 = new Tasca();
        tasca2.setIdentificador("test-tasca-batch-2-" + System.currentTimeMillis());
        tasca2.setAppCodi("NOT");
        tasca2.setEntornCodi("DEV");
        tasca2.setNom("Tasca de prova 2");
        tasca2.setDescripcio("Segona tasca del batch");
        tasca2.setTipus("PROVA");
        tasca2.setEstat(TascaEstat.PENDENT);
        tasca2.setEstatDescripcio("Pendent");
        tasca2.setDataInici(OffsetDateTime.now());
        tasca2.setDataCaducitat(OffsetDateTime.now().plusDays(10));
        tasca2.setUsuarisAmbPermis(List.of("com_admin"));
        tasca2.setPrioritat(Prioritat.ALTA);
        tasca2.setRedireccio(new URL("http://localhost:8080/tasca/mock"));

        List<Tasca> tasques = Arrays.asList(tasca1, tasca2);
        String res = api.crearMultiplesTasques(tasques);
        System.out.println("Batch create response: " + res);
        assertThat(res).isNotNull();
    }
}
