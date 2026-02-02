package es.caib.comanda.api.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.api.v1.management.AppComandaPermisosApi;
import org.junit.jupiter.api.Assumptions;

public class PermisApiExternalIT {

    private static AppComandaPermisosApi api;
    private static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//    @BeforeAll
    static void setup() {
        System.setProperty("javax.net.ssl.trustStore", "/home/siona/Feina/Server/webapps/config/keystores/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "******");
        System.setProperty(ClientProps2.PROP_BASE, "https://dev.caib.es/notibapi/interna");
        System.setProperty(ClientProps2.PROP_USER, "$com_not");
        System.setProperty(ClientProps2.PROP_PWD, "******");

        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps2.PROP_BASE + "=<baseUrl> to run these tests");
        api = ApiClientFactory.permisosApi();
    }

//    @Test
//    void consultarPermis_ok() throws Exception {
//        // Note: This test requires a valid permission to exist
//        // You may need to adjust the test data based on your environment
//        String identificador = "test-permis-id";
//        String appCodi = "TEST_APP";
//        String entornCodi = "DEV";
//
//        try {
//            Permis res = api.consultarPermis(identificador, appCodi, entornCodi);
//            System.out.println(objectMapper.writeValueAsString(res));
//            assertThat(res).isNotNull();
//            assertThat(res.getIdentificador()).isEqualTo(identificador);
//        } catch (Exception e) {
//            // If permission doesn't exist, skip the test
//            Assumptions.assumeTrue(false, "Permission not found: " + e.getMessage());
//        }
//    }
//
//    @Test
//    void crearPermis_ok() throws Exception {
//        Permis permis = new Permis();
//        permis.setIdentificador("test-permis-" + System.currentTimeMillis());
//        permis.setAppCodi("TEST_APP");
//        permis.setEntornCodi("DEV");
//        permis.setUsuari("test-user");
//        permis.setRolCodi("USER");
//
//        String res = api.crearPermis(permis);
//        System.out.println("Create response: " + res);
//        assertThat(res).isNotNull();
//    }
//
//    @Test
//    void crearMultiplesPermisos_ok() throws Exception {
//        Permis permis1 = new Permis();
//        permis1.setIdentificador("test-permis-batch-1-" + System.currentTimeMillis());
//        permis1.setAppCodi("TEST_APP");
//        permis1.setEntornCodi("DEV");
//        permis1.setUsuariCodi("test-user-1");
//        permis1.setRolCodi("USER");
//
//        Permis permis2 = new Permis();
//        permis2.setIdentificador("test-permis-batch-2-" + System.currentTimeMillis());
//        permis2.setAppCodi("TEST_APP");
//        permis2.setEntornCodi("DEV");
//        permis2.setUsuariCodi("test-user-2");
//        permis2.setRolCodi("ADMIN");
//
//        List<Permis> permisos = Arrays.asList(permis1, permis2);
//        String res = api.crearMultiplesPermisos(permisos);
//        System.out.println("Batch create response: " + res);
//        assertThat(res).isNotNull();
//    }
}
