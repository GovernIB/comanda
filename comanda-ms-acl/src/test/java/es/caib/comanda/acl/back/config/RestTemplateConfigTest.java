package es.caib.comanda.acl.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

    @Test
    void restTemplate_configuraUnConverterJacksonPersonalitzat() {
        // Comprova que el RestTemplate injecta un converter Jackson amb els flags esperats.
        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate restTemplate = config.restTemplate(new RestTemplateBuilder());
        MappingJackson2HttpMessageConverter converter = (MappingJackson2HttpMessageConverter) restTemplate.getMessageConverters().get(0);

        assertThat(converter.getObjectMapper().isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).isFalse();
        assertThat(converter.getObjectMapper().isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)).isFalse();
    }
}
