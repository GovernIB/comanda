package es.caib.comanda.api.test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class ApiClientFactory {

    private static String basicAuthHeader(String user, String password) {
        if (user == null || user.isBlank() || password == null) return null;
        String raw = user + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private static String resolveAuthHeader() {
        String header = ClientProps2.get(ClientProps2.PROP_AUTH).orElse(null);
        if (header != null && !header.isBlank()) return header;
        String user = ClientProps2.get(ClientProps2.PROP_USER).orElse(null);
        String pwd = ClientProps2.get(ClientProps2.PROP_PWD).orElse(null);
        return basicAuthHeader(user, pwd);
    }

    public static es.caib.comanda.api.v1.salut.ComandaAppSalutApi salutApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.salut.ApiClient c = new es.caib.comanda.service.v1.salut.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new es.caib.comanda.api.v1.salut.ComandaAppSalutApi(c);
    }

    public static es.caib.comanda.api.v1.estadistica.ComandaAppEstadstiquesApi estadisticaApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.estadistica.ApiClient c = new es.caib.comanda.service.v1.estadistica.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new es.caib.comanda.api.v1.estadistica.ComandaAppEstadstiquesApi(c);
    }

    public static es.caib.comanda.api.v1.log.ComandaAppLogsApi logApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.log.ApiClient c = new es.caib.comanda.service.v1.log.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new es.caib.comanda.api.v1.log.ComandaAppLogsApi(c);
    }
}
