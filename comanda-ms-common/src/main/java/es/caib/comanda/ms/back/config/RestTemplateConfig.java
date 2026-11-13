package es.caib.comanda.ms.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    private final int connectTimeoutSeconds = 5;
    private final int shortReadTimeoutSeconds = 10;
    private final int mediumReadTimeoutSeconds = 60;
    private final int longReadTimeoutSeconds = 600;

    /**
     * Client RestTemplate de nivell MITJÀ (per defecte / @Primary).
     */
    @Bean(name = {"restTemplate", "mediumTimeoutRestTemplate"})
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .setReadTimeout(Duration.ofSeconds(mediumReadTimeoutSeconds))
                .build();
        restTemplate.getMessageConverters().add(0, getConverter());
        return restTemplate;
    }

    /**
     * Client RestTemplate de nivell CURT (fail-fast).
     */
    @Bean(name = {"shortTimeoutRestTemplate"})
    public RestTemplate shortTimeoutRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .setReadTimeout(Duration.ofSeconds(shortReadTimeoutSeconds))
                .build();
        restTemplate.getMessageConverters().add(0, getConverter());
        return restTemplate;
    }

    /**
     * Client RestTemplate de nivell LLARG (operacions pesades).
     */
    @Bean(name = {"longTimeoutRestTemplate"})
    public RestTemplate longTimeoutRestTemplate(RestTemplateBuilder builder) {
        RestTemplateBuilder b = builder.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        if (longReadTimeoutSeconds > 0) {
            b = b.setReadTimeout(Duration.ofSeconds(longReadTimeoutSeconds));
        }
        RestTemplate restTemplate = b.build();
        restTemplate.getMessageConverters().add(0, getConverter());
        return restTemplate;
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
