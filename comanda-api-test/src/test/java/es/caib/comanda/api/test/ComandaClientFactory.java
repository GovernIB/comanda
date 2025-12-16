package es.caib.comanda.api.test;

import es.caib.comanda.api.client.v1.ComandaClient;

import static es.caib.comanda.api.test.ClientProps.*;

final class ComandaClientFactory {
    static ComandaClient create() {
        String base = ClientProps.get(PROP_BASE).orElse(null);
        if (base == null) return null;

        String authHeader = ClientProps.get(PROP_AUTH).orElse(null);
        if (authHeader != null) {
            return ComandaClient.builder(base)
                    .authorization(authHeader)
                    .build();
        }
        String user = ClientProps.get(PROP_USER).orElse(null);
        String pwd = ClientProps.get(PROP_PWD).orElse(null);
        if (user != null && pwd != null) {
            return new ComandaClient(base, user, pwd);
        }
        // Without auth
        return ComandaClient.builder(base).build();
    }
}
