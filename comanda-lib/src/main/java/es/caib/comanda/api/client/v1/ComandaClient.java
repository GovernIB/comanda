package es.caib.comanda.api.client.v1;

import es.caib.comanda.api.v1.avis.AppComandaAvisosApi;
import es.caib.comanda.api.v1.permis.AppComandaPermisosApi;
import es.caib.comanda.api.v1.tasca.AppComandaTasquesApi;
import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.avis.AvisPage;
import es.caib.comanda.model.v1.permis.Permis;
import es.caib.comanda.model.v1.tasca.Tasca;
import es.caib.comanda.model.v1.tasca.TascaPage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Client unificat per enviar missatges a les cues de COMANDA (tasques, avisos, permisos)
 * utilitzant els clients generats i carregats via Maven.
 */
public class ComandaClient {

    private final AppComandaAvisosApi avisosApi;
    private final AppComandaPermisosApi permisosApi;
    private final AppComandaTasquesApi tasquesApi;

    /**
     * Nou constructor que permet passar {@code basePath}, {@code user} i {@code password}.
     * Si {@code user} i {@code password} no són buits, s'aplica autenticació Basic
     * via capçalera {@code Authorization} als clients generats.
     */
    public ComandaClient(String basePath, String user, String password) {
        this(new Builder(basePath).authorization(basicAuthHeader(user, password)));
    }

    private ComandaClient(Builder b) {
        es.caib.comanda.service.v1.avis.ApiClient avisClient = new es.caib.comanda.service.v1.avis.ApiClient();
        avisClient.setBasePath(b.basePath);
        if (b.authorization != null && !b.authorization.isBlank()) {
            avisClient.addDefaultHeader("Authorization", b.authorization);
        }
        this.avisosApi = new AppComandaAvisosApi(avisClient);

        es.caib.comanda.service.v1.permis.ApiClient permisClient = new es.caib.comanda.service.v1.permis.ApiClient();
        permisClient.setBasePath(b.basePath);
        if (b.authorization != null && !b.authorization.isBlank()) {
            permisClient.addDefaultHeader("Authorization", b.authorization);
        }
        this.permisosApi = new AppComandaPermisosApi(permisClient);

        es.caib.comanda.service.v1.tasca.ApiClient tascaClient = new es.caib.comanda.service.v1.tasca.ApiClient();
        tascaClient.setBasePath(b.basePath);
        if (b.authorization != null && !b.authorization.isBlank()) {
            tascaClient.addDefaultHeader("Authorization", b.authorization);
        }
        this.tasquesApi = new AppComandaTasquesApi(tascaClient);
    }

    public String crearAvis(Avis avis) throws Exception {
        return avisosApi.crearAvis(avis);
    }

    public Avis consultarAvis(String identificador, String appCodi, String entornCodi) throws Exception {
        return avisosApi.consultarAvis(identificador, appCodi, entornCodi);
    }

    public String crearMultiplesAvisos(List<Avis> avisos) throws Exception {
        return avisosApi.crearMultiplesAvisos(avisos);
    }

    public String modificarAvis(String identificador, Avis avis) throws Exception {
        return avisosApi.modificarAvis(identificador, avis);
    }

    public String modificarMultiplesAvisos(List<Avis> avisos) throws Exception {
        return avisosApi.modificarMultiplesAvisos(avisos);
    }

    public AvisPage obtenirLlistatAvisos(String quickFilter, String filter, String page, Integer size) throws Exception {
        return avisosApi.obtenirLlistatAvisos(quickFilter, filter, null, null, page, size);
    }

    public String crearPermis(Permis permis) throws Exception {
        return permisosApi.crearPermis(permis);
    }

    public Permis consultarPermis(String identificador, String appCodi, String entornCodi) throws Exception {
        return permisosApi.consultarPermis(identificador, appCodi, entornCodi);
    }

    public String crearMultiplesPermisos(List<Permis> permisos) throws Exception {
        return permisosApi.crearMultiplesPermisos(permisos);
    }

    public String eliminarPermisos(List<Permis> permisos) throws Exception {
        return permisosApi.eliminarPermisos(permisos);
    }

    public String modificarPermis(String identificador, Permis permis) throws Exception {
        return permisosApi.modificarPermis(identificador, permis);
    }

    public String modificarMultiplesPermisos(List<Permis> permisos) throws Exception {
        return permisosApi.modificarMultiplesPermisos(permisos);
    }

    public String crearTasca(Tasca tasca) throws Exception {
        return tasquesApi.crearTasca(tasca);
    }

    public Tasca consultarTasca(String identificador, String appCodi, String entornCodi) throws Exception {
        return tasquesApi.consultarTasca(identificador, appCodi, entornCodi);
    }

    public String crearMultiplesTasques(List<Tasca> tasques) throws Exception {
        return tasquesApi.crearMultiplesTasques(tasques);
    }

    public String modificarTasca(String identificador, Tasca tasca) throws Exception {
        return tasquesApi.modificarTasca(identificador, tasca);
    }

    public String modificarMultiplesTasques(List<Tasca> tasques) throws Exception {
        return tasquesApi.modificarMultiplesTasques(tasques);
    }

    public TascaPage obtenirLlistatTasques(String quickFilter, String filter, String page, Integer size) throws Exception {
        return tasquesApi.obtenirLlistatTasques(quickFilter, filter, null, null, page, size);
    }

    public static Builder builder(String basePath) { return new Builder(basePath); }

    public static class Builder {
        private final String basePath;
        private String authorization;

        public Builder(String basePath) { this.basePath = basePath; }

        /**
         * Valor complet de la capçalera Authorization (p. ex. "Bearer abc...")
         */
        public Builder authorization(String authorizationHeaderValue) {
            this.authorization = authorizationHeaderValue;
            return this;
        }

        public ComandaClient build() { return new ComandaClient(this); }
    }

    private static String basicAuthHeader(String user, String password) {
        if (user == null || user.isBlank() || password == null) return null;
        String raw = user + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
