package es.caib.comanda.ms.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

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
}
