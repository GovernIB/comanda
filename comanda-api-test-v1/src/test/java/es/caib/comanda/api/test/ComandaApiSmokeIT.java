package es.caib.comanda.api.test;

import es.caib.comanda.model.management.Avis;
import es.caib.comanda.model.management.Objecte;
import es.caib.comanda.model.management.Permis;
import es.caib.comanda.model.management.Tasca;
import es.caib.comanda.model.management.Usuari;
import es.caib.comanda.service.management.AppComandaClient;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComandaApiSmokeIT {

    private static AppComandaClient client;

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
        Avis avis = new Avis()
                .appCodi("APP")
                .entornCodi("DEV")
                .identificador("AV-" + System.currentTimeMillis())
                .nom("Prova avís")
                .descripcio("Descripció");
        String res = client.crearAvis(avis);
        assertThat(res).isNotNull();
    }

    @Test
    void sendTasca_ok() throws Exception {
        Tasca tasca = new Tasca()
                .appCodi("APP")
                .entornCodi("DEV")
                .identificador("TAS-" + System.currentTimeMillis())
                .nom("Prova tasca")
                .tipus("PROVA");
        String res = client.crearTasca(tasca);
        assertThat(res).isNotNull();
    }

    @Test
    void sendPermis_ok() throws Exception {
        Permis permis = new Permis()
                .appCodi("APP")
                .entornCodi("DEV")
                .usuari(new Usuari().codi("usr1").nom("Usuari 1"))
                .objecte(new Objecte().tipus("EXPEDIENT").identificador("EXP-" + System.currentTimeMillis()));
        String res = client.crearPermis(permis);
        assertThat(res).isNotNull();
    }
}
