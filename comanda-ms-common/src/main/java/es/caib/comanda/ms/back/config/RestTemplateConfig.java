package es.caib.comanda.ms.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@Slf4j
@Configuration
public class RestTemplateConfig {

    static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
    static final int DEFAULT_SHORT_READ_TIMEOUT_SECONDS = 10;
    static final int DEFAULT_MEDIUM_READ_TIMEOUT_SECONDS = 60;
    static final int DEFAULT_LONG_READ_TIMEOUT_SECONDS = 600;

    private static final ThreadLocal<Boolean> RESOLVING_TIMEOUT = new ThreadLocal<>();

    private Supplier<ParametresHelper> parametresHelperSupplier = () -> null;

    public RestTemplateConfig() {
    }

    public RestTemplateConfig(ParametresHelper parametresHelper) {
        this.parametresHelperSupplier = () -> parametresHelper;
    }

    @Autowired
    void setParametresHelperProvider(ObjectProvider<ParametresHelper> parametresHelperProvider) {
        this.parametresHelperSupplier = parametresHelperProvider::getIfAvailable;
    }

    /**
     * Client RestTemplate de nivell MITJÀ (per defecte / @Primary).
     */
    @Bean(name = {"restTemplate", "mediumTimeoutRestTemplate"})
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return buildRestTemplate(
                builder,
                BaseConfig.PROP_HTTP_READ_TIMEOUT_MEDIUM_SECONDS,
                DEFAULT_MEDIUM_READ_TIMEOUT_SECONDS);
    }

    /**
     * Client RestTemplate de nivell CURT (fail-fast).
     */
    @Bean(name = {"shortTimeoutRestTemplate"})
    public RestTemplate shortTimeoutRestTemplate(RestTemplateBuilder builder) {
        return buildRestTemplate(
                builder,
                BaseConfig.PROP_HTTP_READ_TIMEOUT_SHORT_SECONDS,
                DEFAULT_SHORT_READ_TIMEOUT_SECONDS);
    }

    /**
     * Client RestTemplate de nivell LLARG (operacions pesades).
     */
    @Bean(name = {"longTimeoutRestTemplate"})
    public RestTemplate longTimeoutRestTemplate(RestTemplateBuilder builder) {
        return buildRestTemplate(
                builder,
                BaseConfig.PROP_HTTP_READ_TIMEOUT_LONG_SECONDS,
                DEFAULT_LONG_READ_TIMEOUT_SECONDS);
    }

    private RestTemplate buildRestTemplate(
            RestTemplateBuilder builder,
            String readTimeoutCode,
            int defaultReadTimeoutSeconds) {
        RestTemplate restTemplate = builder
                .requestFactory(() -> new DynamicTimeoutRequestFactory(
                        () -> resolveTimeoutSeconds(
                                BaseConfig.PROP_HTTP_CONNECT_TIMEOUT_SECONDS,
                                DEFAULT_CONNECT_TIMEOUT_SECONDS),
                        () -> resolveTimeoutSeconds(
                                readTimeoutCode,
                                defaultReadTimeoutSeconds)))
                .build();
        restTemplate.getMessageConverters().add(0, getConverter());
        return restTemplate;
    }

    private int resolveTimeoutSeconds(String codi, int defaultSeconds) {
        ParametresHelper parametresHelper = parametresHelperSupplier.get();
        if (parametresHelper == null || Boolean.TRUE.equals(RESOLVING_TIMEOUT.get())) {
            return defaultSeconds;
        }
        RESOLVING_TIMEOUT.set(true);
        try {
            Integer value = parametresHelper.getParametreEnter(codi, defaultSeconds);
            return value != null ? value : defaultSeconds;
        } catch (Exception e) {
            log.warn("No s'ha pogut resoldre el paràmetre de timeout '{}'. S'utilitza el valor per defecte ({}s): {}",
                    codi, defaultSeconds, e.getMessage());
            return defaultSeconds;
        } finally {
            RESOLVING_TIMEOUT.remove();
        }
    }

    /**
     * RequestFactory que resol i aplica els timeouts de connexió i lectura dinàmicament per a cada petició.
     */
    static class DynamicTimeoutRequestFactory extends SimpleClientHttpRequestFactory {

        private final IntSupplier connectTimeoutSecondsSupplier;
        private final IntSupplier readTimeoutSecondsSupplier;

        DynamicTimeoutRequestFactory(
                IntSupplier connectTimeoutSecondsSupplier,
                IntSupplier readTimeoutSecondsSupplier) {
            this.connectTimeoutSecondsSupplier = connectTimeoutSecondsSupplier;
            this.readTimeoutSecondsSupplier = readTimeoutSecondsSupplier;
        }

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            super.prepareConnection(connection, httpMethod);
            int connectTimeoutSec = connectTimeoutSecondsSupplier.getAsInt();
            if (connectTimeoutSec >= 0) {
                connection.setConnectTimeout(toMillis(connectTimeoutSec));
            }
            int readTimeoutSec = readTimeoutSecondsSupplier.getAsInt();
            if (readTimeoutSec > 0) {
                connection.setReadTimeout(toMillis(readTimeoutSec));
            } else {
                connection.setReadTimeout(0);
            }
        }

        private static int toMillis(int seconds) {
            return (int) Math.min((long) seconds * 1000L, Integer.MAX_VALUE);
        }
    }

    /**
     * Configura i retorna un {@link MappingJackson2HttpMessageConverter} personalitzat amb un {@link ObjectMapper}
     * per gestionar la serialització i deserialització de dades JSON (HAL, Java 8 Date/Time, subtipus i tipus abstractes).
     *
     * @return instància de {@link MappingJackson2HttpMessageConverter} configurada.
     */
    public static MappingJackson2HttpMessageConverter getConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jackson2HalModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Dimensio.class, GenericDimensio.class);
        module.addAbstractTypeMapping(Fet.class, GenericFet.class);
        objectMapper.registerModule(module);

        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
