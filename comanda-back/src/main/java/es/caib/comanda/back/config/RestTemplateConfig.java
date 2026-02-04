package es.caib.comanda.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
//                .setReadTimeout(Duration.ofSeconds(10))     // timeout de lectura
                .additionalMessageConverters(getConverter())
                .build();
//        restTemplate.getMessageConverters().add(0, getConverter());
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
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Dimensio.class, GenericDimensio.class);
        module.addAbstractTypeMapping(Fet.class, GenericFet.class);
        objectMapper.registerModule(module);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        return converter;
    }

}
