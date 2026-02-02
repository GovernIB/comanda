package es.caib.comanda.api.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;
import es.caib.comanda.model.v1.management.Avis;
import es.caib.comanda.model.v1.management.AvisPage;
import es.caib.comanda.model.v1.management.AvisTipus;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AvisApiExternalIT {

    private static AppComandaAvisosApi api;
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
        api = ApiClientFactory.avisosApi();
    }

    @Test
    void obtenirLlistatAvisos_ok() throws Exception {
        AvisPage res = api.obtenirLlistatAvisos(null, null, null, null, null, 10);
        System.out.println(objectMapper.writeValueAsString(res));
        assertThat(res).isNotNull();
        assertThat(res.getContent()).isNotNull();
    }

    @Test
    void consultarAvis_ok() throws Exception {
        // First get a list to obtain a valid notification id
        AvisPage page = api.obtenirLlistatAvisos(null, null, null, null, null, 1);
        Assumptions.assumeTrue(page.getContent() != null && !page.getContent().isEmpty(),
                "No notifications available for testing");

        Avis firstAvis = page.getContent().get(0);
        String identificador = firstAvis.getIdentificador();
        String appCodi = firstAvis.getAppCodi();
        String entornCodi = firstAvis.getEntornCodi();

        Avis res = api.consultarAvis(identificador, appCodi, entornCodi);
        System.out.println(objectMapper.writeValueAsString(res));
        assertThat(res).isNotNull();
        assertThat(res.getIdentificador()).isEqualTo(identificador);
    }

    @Test
    void crearAvis_ok() throws Exception {
        Avis avis = new Avis();
        avis.setIdentificador("test-avis-" + System.currentTimeMillis());
        avis.setAppCodi("NOT");
        avis.setEntornCodi("DEV");
        avis.setNom("Avís de prova");
        avis.setDescripcio("Descripció de l'avís de prova");
        avis.setDataInici(OffsetDateTime.now());
        avis.setDataFi(OffsetDateTime.now().plusDays(10));
        avis.setTipus(AvisTipus.INFO);
        avis.setResponsable("com_admin");

        String res = api.crearAvis(avis);
        System.out.println("Create response: " + res);
        assertThat(res).isNotNull();
    }

    @Test
    void crearAvis_error() {
        Avis avis = new Avis();
        avis.setIdentificador("test-avis-" + System.currentTimeMillis());
        avis.setAppCodi("NOT");
        avis.setEntornCodi("DEV");
        avis.setNom("Avís de prova");
        avis.setDescripcio("Descripció de l'avís de prova");
        avis.setDataInici(OffsetDateTime.now());
        avis.setDataFi(OffsetDateTime.now().plusDays(10));
//        avis.setTipus(AvisTipus.INFO);

        assertThatThrownBy(() -> api.crearAvis(avis))
                .hasMessageContaining("Cal informar els camps identificador, appCodi, entornCodi, nom i tipus");
    }

    @Test
    void crearMultiplesAvisos_ok() throws Exception {
        Avis avis1 = new Avis();
        avis1.setIdentificador("test-avis-batch-1-" + System.currentTimeMillis());
        avis1.setAppCodi("NOT");
        avis1.setEntornCodi("DEV");
        avis1.setNom("Avís de prova 1");
        avis1.setDescripcio("Primer avís del batch");
        avis1.setDataInici(OffsetDateTime.now());
        avis1.setTipus(AvisTipus.INFO);
        avis1.setResponsable("com_admin");

        Avis avis2 = new Avis();
        avis2.setIdentificador("test-avis-batch-2-" + System.currentTimeMillis());
        avis2.setAppCodi("NOT");
        avis2.setEntornCodi("DEV");
        avis2.setNom("Avís de prova 2");
        avis2.setDescripcio("Segon avís del batch");
        avis2.setDataInici(OffsetDateTime.now());
        avis2.setDataFi(OffsetDateTime.now().plusDays(10));
        avis2.setTipus(AvisTipus.INFO);

        List<Avis> avisos = Arrays.asList(avis1, avis2);
        String res = api.crearMultiplesAvisos(avisos);
        System.out.println("Batch create response: " + res);
        assertThat(res).isNotNull();
    }
}
