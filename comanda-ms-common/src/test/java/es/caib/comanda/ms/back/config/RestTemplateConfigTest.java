package es.caib.comanda.ms.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ParametreClientHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestTemplateConfigTest {

    private static final URI TEST_URI = URI.create("http://localhost/test");

    @Test
    void restTemplate_creaInstanciaMitjanaAmbConverterComplet() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate restTemplate = config.restTemplate(new RestTemplateBuilder());

        assertThat(restTemplate.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
        ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) restTemplate.getMessageConverters().get(0)).getObjectMapper();

        assertThat(objectMapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310", "json-hal-module");
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).isFalse();
        assertThat(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)).isFalse();
    }

    @Test
    void shortTimeoutRestTemplate_creaInstanciaCurta() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate restTemplate = config.shortTimeoutRestTemplate(new RestTemplateBuilder());

        assertThat(restTemplate.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
    }

    @Test
    void longTimeoutRestTemplate_creaInstanciaLlarga() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate restTemplate = config.longTimeoutRestTemplate(new RestTemplateBuilder());

        assertThat(restTemplate.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
    }

    @Test
    void getConverter_mapejaTipusAbstractesAImplementacionsGeneriques() throws Exception {
        MappingJackson2HttpMessageConverter converter = RestTemplateConfig.getConverter();
        ObjectMapper objectMapper = converter.getObjectMapper();

        Object dimensio = objectMapper.readValue("{}", Dimensio.class);
        Object fet = objectMapper.readValue("{}", Fet.class);

        assertThat(dimensio).isInstanceOf(GenericDimensio.class);
        assertThat(fet).isInstanceOf(GenericFet.class);
    }

    @Test
    void requestFactory_senseParametresHelper_aplicaTimeoutsPerDefecte() throws Exception {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplateBuilder builder = new RestTemplateBuilder();

        HttpURLConnection mediumConn = openPreparedConnection(config.restTemplate(builder));
        HttpURLConnection shortConn = openPreparedConnection(config.shortTimeoutRestTemplate(builder));
        HttpURLConnection longConn = openPreparedConnection(config.longTimeoutRestTemplate(builder));

        assertThat(mediumConn.getConnectTimeout()).isEqualTo(5000);
        assertThat(mediumConn.getReadTimeout()).isEqualTo(60000);

        assertThat(shortConn.getConnectTimeout()).isEqualTo(5000);
        assertThat(shortConn.getReadTimeout()).isEqualTo(10000);

        assertThat(longConn.getConnectTimeout()).isEqualTo(5000);
        assertThat(longConn.getReadTimeout()).isEqualTo(600000);
    }

    @Test
    void requestFactory_quanCanvienParametresEnExecucio_actualitzaTimeoutsDinamicament() throws Exception {
        ParametresHelper parametresHelper = mock(ParametresHelper.class);
        RestTemplateConfig config = new RestTemplateConfig(parametresHelper);
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate mediumRestTemplate = config.restTemplate(builder);
        RestTemplate shortRestTemplate = config.shortTimeoutRestTemplate(builder);
        RestTemplate longRestTemplate = config.longTimeoutRestTemplate(builder);

        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_CONNECT_TIMEOUT_SECONDS), anyInt())).thenReturn(7);
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_MEDIUM_SECONDS), anyInt())).thenReturn(25);
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_SHORT_SECONDS), anyInt())).thenReturn(4);
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_LONG_SECONDS), anyInt())).thenReturn(900);

        HttpURLConnection firstMedium = openPreparedConnection(mediumRestTemplate);
        HttpURLConnection firstShort = openPreparedConnection(shortRestTemplate);
        HttpURLConnection firstLong = openPreparedConnection(longRestTemplate);

        assertThat(firstMedium.getConnectTimeout()).isEqualTo(7000);
        assertThat(firstMedium.getReadTimeout()).isEqualTo(25000);
        assertThat(firstShort.getConnectTimeout()).isEqualTo(7000);
        assertThat(firstShort.getReadTimeout()).isEqualTo(4000);
        assertThat(firstLong.getConnectTimeout()).isEqualTo(7000);
        assertThat(firstLong.getReadTimeout()).isEqualTo(900000);

        // Simulaci\u00f3 d'actualitzaci\u00f3 de par\u00e0metres a BBDD en temps d'execuci\u00f3 sobre els mateixos beans
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_CONNECT_TIMEOUT_SECONDS), anyInt())).thenReturn(12);
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_MEDIUM_SECONDS), anyInt())).thenReturn(90);

        HttpURLConnection updatedMedium = openPreparedConnection(mediumRestTemplate);
        assertThat(updatedMedium.getConnectTimeout()).isEqualTo(12000);
        assertThat(updatedMedium.getReadTimeout()).isEqualTo(90000);
    }

    @Test
    void requestFactory_quanParametresHelperFallaRetornaNullOReadTimeoutZero_aplicaFallbacks() throws Exception {
        ParametresHelper parametresHelper = mock(ParametresHelper.class);
        RestTemplateConfig config = new RestTemplateConfig(parametresHelper);
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate mediumRestTemplate = config.restTemplate(builder);
        RestTemplate longRestTemplate = config.longTimeoutRestTemplate(builder);

        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_CONNECT_TIMEOUT_SECONDS), anyInt()))
                .thenThrow(new RuntimeException("Error de connexi\u00f3"));
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_MEDIUM_SECONDS), anyInt()))
                .thenReturn(null);
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_LONG_SECONDS), anyInt()))
                .thenReturn(0);

        HttpURLConnection mediumConn = openPreparedConnection(mediumRestTemplate);
        assertThat(mediumConn.getConnectTimeout()).isEqualTo(5000);
        assertThat(mediumConn.getReadTimeout()).isEqualTo(60000);

        HttpURLConnection longConnZero = openPreparedConnection(longRestTemplate);
        assertThat(longConnZero.getReadTimeout()).isEqualTo(0);

        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_CONNECT_TIMEOUT_SECONDS), anyInt()))
                .thenReturn(-1);
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_LONG_SECONDS), anyInt()))
                .thenReturn(-5);

        HttpURLConnection longConnNegative = openPreparedConnection(longRestTemplate);
        assertThat(longConnNegative.getConnectTimeout()).isEqualTo(0);
        assertThat(longConnNegative.getReadTimeout()).isEqualTo(0);
    }

    @Test
    void requestFactory_quanResolucioProvoquiCridaNiuada_evitaRecursioInfinita() throws Exception {
        ParametresHelper parametresHelper = mock(ParametresHelper.class);
        RestTemplateConfig config = new RestTemplateConfig(parametresHelper);
        RestTemplate restTemplate = config.restTemplate(new RestTemplateBuilder());

        AtomicInteger connectCalls = new AtomicInteger();
        AtomicReference<HttpURLConnection> nestedConnectionRef = new AtomicReference<>();

        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_CONNECT_TIMEOUT_SECONDS), anyInt()))
                .thenAnswer(invocation -> {
                    connectCalls.incrementAndGet();
                    if (nestedConnectionRef.get() == null) {
                        nestedConnectionRef.set(openPreparedConnection(restTemplate));
                    }
                    return 15;
                });
        when(parametresHelper.getParametreEnter(eq(BaseConfig.PROP_HTTP_READ_TIMEOUT_MEDIUM_SECONDS), anyInt()))
                .thenReturn(45);

        HttpURLConnection outerConnection = openPreparedConnection(restTemplate);

        assertThat(outerConnection.getConnectTimeout()).isEqualTo(15000);
        assertThat(outerConnection.getReadTimeout()).isEqualTo(45000);

        HttpURLConnection nestedConnection = nestedConnectionRef.get();
        assertThat(nestedConnection).isNotNull();
        assertThat(nestedConnection.getConnectTimeout()).isEqualTo(5000);
        assertThat(nestedConnection.getReadTimeout()).isEqualTo(60000);
        assertThat(connectCalls.get()).isEqualTo(1);

        // Despr\u00e9s de finalitzar la resoluci\u00f3 externa, el guard ThreadLocal queda netejat
        HttpURLConnection subsequentConnection = openPreparedConnection(restTemplate);
        assertThat(subsequentConnection.getConnectTimeout()).isEqualTo(15000);
        assertThat(connectCalls.get()).isEqualTo(2);
    }

    @Test
    void springContext_ambParametresHelperIHttpAuthorizationHeaderHelper_noCreaDependenciaCircular() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getDefaultListableBeanFactory().setAllowCircularReferences(false);
            context.register(
                    CircularDependencyTestConfig.class,
                    RestTemplateConfig.class,
                    HttpAuthorizationHeaderHelper.class,
                    ParametreClientHelper.class,
                    ParametresHelper.class);
            context.refresh();

            RestTemplate restTemplate = context.getBean("restTemplate", RestTemplate.class);
            assertThat(restTemplate).isNotNull();
            assertThat(context.getBean(ParametreClientHelper.class)).isNotNull();
            assertThat(context.getBean(ParametresHelper.class)).isNotNull();
            assertThat(context.getBean(HttpAuthorizationHeaderHelper.class)).isNotNull();

            HttpURLConnection connection = openPreparedConnection(restTemplate);
            assertThat(connection.getConnectTimeout()).isEqualTo(5000);
            assertThat(connection.getReadTimeout()).isEqualTo(60000);
        }
    }

    @Configuration
    static class CircularDependencyTestConfig {

        @Bean
        RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder();
        }

        @Bean
        ParametreServiceClient parametreServiceClient() {
            return mock(ParametreServiceClient.class);
        }
    }

    private static HttpURLConnection openPreparedConnection(RestTemplate restTemplate) throws IOException {
        ClientHttpRequest request = restTemplate.getRequestFactory().createRequest(TEST_URI, HttpMethod.GET);
        HttpURLConnection connection = (HttpURLConnection) ReflectionTestUtils.getField(request, "connection");
        assertThat(connection).isNotNull();
        return connection;
    }
}
