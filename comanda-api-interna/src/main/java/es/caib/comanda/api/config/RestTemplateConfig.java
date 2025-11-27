package es.caib.comanda.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.ms.estadistica.model.GenericDimensio;
import es.caib.comanda.ms.estadistica.model.GenericFet;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Lazy
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))   // timeout de connexió
                .setReadTimeout(Duration.ofSeconds(10))     // timeout de lectura
                .build();
        restTemplate.getMessageConverters().add(0, getConverter());
        return restTemplate;
    }

    /**
     * Configura i retorna un {@link MappingJackson2HttpMessageConverter} personalitzat amb un {@link ObjectMapper}
     * per gestionar la serialització i deserialització de dades JSON. El mapejador afegeix configuracions específiques com la desactivació
     * d'errors en subtipus i en objectes buits, i registra un mòdul que mapeja tipus abstractes a implementacions concretes.
     *
     * @return una instància de {@link MappingJackson2HttpMessageConverter} configurada amb ajustos personalitzats.
     */
    private static MappingJackson2HttpMessageConverter getConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jackson2HalModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(es.caib.comanda.ms.estadistica.model.Dimensio.class, GenericDimensio.class);
        module.addAbstractTypeMapping(es.caib.comanda.ms.estadistica.model.Fet.class, GenericFet.class);
        objectMapper.registerModule(module);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        return converter;
    }

}
