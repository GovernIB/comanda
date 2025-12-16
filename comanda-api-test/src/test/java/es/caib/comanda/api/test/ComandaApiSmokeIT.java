package es.caib.comanda.api.test;

import es.caib.comanda.api.client.v1.ComandaClient;
import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.permis.Objecte;
import es.caib.comanda.model.v1.permis.Permis;
import es.caib.comanda.model.v1.permis.Usuari;
import es.caib.comanda.model.v1.tasca.Tasca;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComandaApiSmokeIT {

    private static ComandaClient client;

    @BeforeAll
    static void setUp() {
        System.setProperty(ClientProps.PROP_BASE, "http://localhost:8080/comandaapi/interna");
        System.setProperty(ClientProps.PROP_USER, "com_admin");
        System.setProperty(ClientProps.PROP_PWD, "******");

        String base = ClientProps.get(ClientProps.PROP_BASE).orElse(null);
        Assumptions.assumeTrue(base != null && !base.isBlank(),
                () -> "SKIPPED: define -D" + ClientProps.PROP_BASE + "=<baseUrl> to run these tests");
        client = ComandaClientFactory.create();
    }

    @Test
    void sendAvis_ok() throws Exception {
        Avis avis = Avis.builder()
                .appCodi("APP")
                .entornCodi("DEV")
                .identificador("AV-" + System.currentTimeMillis())
                .nom("Prova avís")
                .descripcio("Descripció")
                .build();
        String res = client.crearAvis(avis);
        assertThat(res).isNotNull();
    }

    @Test
    void sendTasca_ok() throws Exception {
        Tasca tasca = Tasca.builder()
                .appCodi("APP")
                .entornCodi("DEV")
                .identificador("TAS-" + System.currentTimeMillis())
                .nom("Prova tasca")
                .tipus("PROVA")
                .build();
        String res = client.crearTasca(tasca);
        assertThat(res).isNotNull();
    }

    @Test
    void sendPermis_ok() throws Exception {
        Permis permis = Permis.builder()
                .appCodi("APP")
                .entornCodi("DEV")
                .usuari(Usuari.builder().codi("usr1").nom("Usuari 1").build())
                .objecte(Objecte.builder().tipus("EXPEDIENT").identificador("EXP-" + System.currentTimeMillis()).build())
                .build();
        String res = client.crearPermis(permis);
        assertThat(res).isNotNull();
    }
}
