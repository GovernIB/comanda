package es.caib.comanda.api.test;


import es.caib.comanda.service.management.AppComandaClient;

import static es.caib.comanda.api.test.ClientProps.*;

final class ComandaClientFactory {
    static AppComandaClient create() {
        String base = ClientProps.get(PROP_BASE).orElse(null);
        if (base == null) return null;

        String authHeader = ClientProps.get(PROP_AUTH).orElse(null);
        if (authHeader != null) {
            return AppComandaClient.builder(base)
                    .authorization(authHeader)
                    .build();
        }
        String user = ClientProps.get(PROP_USER).orElse(null);
        String pwd = ClientProps.get(PROP_PWD).orElse(null);
        if (user != null && pwd != null) {
            return new AppComandaClient(base, user, pwd);
        }
        // Without auth
        return AppComandaClient.builder(base).build();
    }
}
