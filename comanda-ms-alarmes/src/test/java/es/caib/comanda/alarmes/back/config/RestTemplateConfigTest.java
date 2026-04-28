package es.caib.comanda.alarmes.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

    @Test
    void restTemplate_quanEsConstrueix_afegeixElConverterHalAlPrincipi() {
        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate restTemplate = config.restTemplate(new RestTemplateBuilder());

        assertThat(restTemplate.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
        ObjectMapper objectMapper = ((MappingJackson2HttpMessageConverter) restTemplate.getMessageConverters().get(0)).getObjectMapper();
        assertThat(objectMapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).isFalse();
        assertThat(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)).isFalse();
    }
}
