package es.caib.comanda.api.test;

import es.caib.comanda.api.v1.monitoring.ComandaAppEstadistiquesApi;
import es.caib.comanda.api.v1.monitoring.ComandaAppLogsApi;
import es.caib.comanda.api.v1.monitoring.ComandaAppSalutApi;
import es.caib.comanda.api.v1.management.AppComandaTasquesApi;
import es.caib.comanda.api.v1.management.AppComandaAvisosApi;
import es.caib.comanda.api.v1.management.AppComandaPermisosApi;

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

    public static ComandaAppSalutApi salutApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.monitoring.ApiClient c = new es.caib.comanda.service.v1.monitoring.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new ComandaAppSalutApi(c);
    }

    public static ComandaAppEstadistiquesApi estadisticaApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.monitoring.ApiClient c = new es.caib.comanda.service.v1.monitoring.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new ComandaAppEstadistiquesApi(c);
    }

    public static ComandaAppLogsApi logApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.monitoring.ApiClient c = new es.caib.comanda.service.v1.monitoring.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new ComandaAppLogsApi(c);
    }

    public static AppComandaTasquesApi tasquesApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.management.ApiClient c = new es.caib.comanda.service.v1.management.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new AppComandaTasquesApi(c);
    }

    public static AppComandaAvisosApi avisosApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.management.ApiClient c = new es.caib.comanda.service.v1.management.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new AppComandaAvisosApi(c);
    }

    public static AppComandaPermisosApi permisosApi() {
        String base = ClientProps2.get(ClientProps2.PROP_BASE).orElse(null);
        if (base == null) return null;
        es.caib.comanda.service.v1.management.ApiClient c = new es.caib.comanda.service.v1.management.ApiClient();
        c.setBasePath(base);
        String auth = resolveAuthHeader();
        if (auth != null) c.addDefaultHeader("Authorization", auth);
        return new AppComandaPermisosApi(c);
    }
}
