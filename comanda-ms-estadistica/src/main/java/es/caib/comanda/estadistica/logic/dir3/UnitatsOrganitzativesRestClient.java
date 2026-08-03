package es.caib.comanda.estadistica.logic.dir3;

import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.MonitorDir3;
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

    @Value("${es.caib.comanda.estadistica.dir3.govern.codi.arrel:}")
    private String codiArrel;

    @Value("${es.caib.comanda.plugin.unitats.organitzatives.dir3.service.url:}")
    private String baseUrl;
    @Value("${es.caib.comanda.plugin.unitats.organitzatives.dir3.service.username:}")
    private String username;
    @Value("${es.caib.comanda.plugin.unitats.organitzatives.dir3.service.password:}")
    private String password;

    private final EstadisticaClientHelper estadisticaClientHelper;
    private final RestTemplate restTemplate;

    private String URL_GET_ONE;
    private String URL_FIND;

    @PostConstruct
    public void init() {
        this.URL_GET_ONE = baseUrl + "/obtenerUnidad";
        this.URL_FIND = baseUrl + "/obtenerArbolUnidadesDestinatarias";
    }


    private String basicAuthHeader(String user, String password) {
        String token = java.util.Base64.getEncoder().encodeToString((user + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private URI uriBuild(String url, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(URI.create(url));
        for (Map.Entry<String, Object> p :params.entrySet()) {
            builder.queryParam(p.getKey(), p.getValue());
        }
        return builder.build(true).toUri();
    }

    private MonitorDir3 initializeMonitor(String url) {
        return new MonitorDir3(url, estadisticaClientHelper);
    }

    public UnidadRest obtenerUnidad(String codigo, String fechaActualizacion, String fechaSincronizacion, Boolean denominacioCooficial) throws SistemaExternException {
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

    public List<UnidadRest> findUnidadArrel(String fechaActualizacion, String fechaSincronizacion, Boolean denominacioCooficial) throws SistemaExternException {
        return this.findUnidad(codiArrel, fechaActualizacion, fechaSincronizacion, denominacioCooficial);
    }

    public List<UnidadRest> findUnidad(String codigo, String fechaActualizacion, String fechaSincronizacion, Boolean denominacioCooficial) throws SistemaExternException {
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
                new ParameterizedTypeReference<List<UnidadRest>>() {});

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
