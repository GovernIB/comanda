package es.caib.comanda.estadistica.logic.dir3;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.MonitorDir3;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnitatsOrganitzativesRestClient {

    /**
     * Codi Dir3 arrel per defecte quan un fet no té entitat associada, o no s'ha configurat cap paràmetre.
     */
    public static final String CODI_ARREL_PER_DEFECTE = "A04003003";

    private final ParametresHelper parametresHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final RestTemplate restTemplate;

    public String getCodiArrel() {
        if (parametresHelper != null) {
            String valor = parametresHelper.getParametreText(BaseConfig.PROP_DIR3_GOVERN_CODI_ARREL);
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return CODI_ARREL_PER_DEFECTE;
    }

    @Value("${" + BaseConfig.PROP_DIR3_SERVICE_URL + ":}")
    private String baseUrl;
    @Value("${" + BaseConfig.PROP_DIR3_SERVICE_USERNAME + ":}")
    private String username;
    @Value("${" + BaseConfig.PROP_DIR3_SERVICE_PASSWORD + ":}")
    private String password;

    private String URL_GET_ONE;
    private String URL_FIND;

    @PostConstruct
    public void init() {
        this.URL_GET_ONE = baseUrl + "/rest/unidades/obtenerUnidad";
        this.URL_FIND = baseUrl + "/rest/unidades/obtenerArbolUnidadesDestinatarias";
    }

    /**
     * Indica si el plugin està correctament configurat (URL absoluta no buida). Es fa servir per evitar continuar
     * fent peticions a Dir3 quan no ho està - vegeu {@link #obtenerUnidad} i {@link #findUnidad}.
     */
    public boolean isConfigured() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return false;
        }
        try {
            return URI.create(baseUrl).isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }


    private String basicAuthHeader(String user, String password) {
        String token = java.util.Base64.getEncoder().encodeToString((user + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private URI uriBuild(String url, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(url));
        for (Map.Entry<String, Object> p : params.entrySet()) {
            builder.queryParam(p.getKey(), p.getValue());
        }
        return builder.build(true).toUri();
    }

    private MonitorDir3 initializeMonitor(String url) {
        return new MonitorDir3(url, estadisticaClientHelper);
    }

    public UnidadRest obtenerUnidad(String codigo,
                                    String fechaActualizacion,
                                    String fechaSincronizacion,
                                    Boolean denominacioCooficial) throws SistemaExternException {
        if (!isConfigured()) {
            throw new SistemaExternException("El plugin d'unitats organitzatives Dir3 no està configurat (URL buida o no vàlida)");
        }
        MonitorDir3 monitor = initializeMonitor(URL_GET_ONE);

        try {
            monitor.startInfoAction();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", basicAuthHeader(username, password));
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            Map<String, Object> params = new HashMap<>();
            params.put("codigo", codigo);
            params.put("denominacionCooficial", denominacioCooficial);
            if (fechaActualizacion != null) params.put("fechaActualizacion", fechaActualizacion);
            if (fechaSincronizacion != null) params.put("fechaSincronizacion", fechaSincronizacion);

            ResponseEntity<UnidadRest> response = restTemplate.exchange(
                uriBuild(URL_GET_ONE, params),
                HttpMethod.GET,
                httpEntity,
                UnidadRest.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                throw new RuntimeException("La unitat organitzativa no està vigent (" + "codi=" + codigo + ")");
            }

            monitor.endInfoAction();
            return response.getBody();
        } catch (Exception ex) {
            monitor.endInfoAction(ex);
            throw new SistemaExternException(ex);
        }
    }

    public List<UnidadRest> findUnidadArrel(String fechaActualizacion,
                                            String fechaSincronizacion,
                                            Boolean denominacioCooficial) throws SistemaExternException {
        return this.findUnidad(getCodiArrel(), fechaActualizacion, fechaSincronizacion, denominacioCooficial);
    }

    public List<UnidadRest> findUnidad(String codigo,
                                       String fechaActualizacion,
                                       String fechaSincronizacion,
                                       Boolean denominacioCooficial) throws SistemaExternException {
        if (!isConfigured()) {
            throw new SistemaExternException("El plugin d'unitats organitzatives Dir3 no està configurat (URL buida o no vàlida)");
        }
        MonitorDir3 monitor = initializeMonitor(URL_FIND);

        try {
            monitor.startOrganigramaAction();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", basicAuthHeader(username, password));
            HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

            Map<String, Object> params = new HashMap<>();
            params.put("codigo", codigo);
            params.put("denominacionCooficial", denominacioCooficial);
            if (fechaActualizacion != null) params.put("fechaActualizacion", fechaActualizacion);
            if (fechaSincronizacion != null) params.put("fechaSincronizacion", fechaSincronizacion);

            ResponseEntity<List<UnidadRest>> response = restTemplate.exchange(
                uriBuild(URL_FIND, params),
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<List<UnidadRest>>() {
                });

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                throw new RuntimeException("La unitat organitzativa no està vigent (" + "codi=" + codigo + ")");
            }

            monitor.endOrganigramaAction();
            return response.getBody();
        } catch (Exception ex) {
            monitor.endOrganigramaAction(ex);
            throw new SistemaExternException(ex);
        }
    }

}
