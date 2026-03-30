package es.caib.comanda.permisos.back.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

    @Test
    void restTemplate_afegeixElConverterPersonalitzatEnPrimeraPosicio() {
        // Verifica que el RestTemplate del mòdul injecta el converter Jackson personalitzat al davant de la llista.
        RestTemplateConfig config = new RestTemplateConfig();

        RestTemplate restTemplate = config.restTemplate(new RestTemplateBuilder());

        assertThat(restTemplate.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
    }

    @Test
    void getConverter_mapejaTipusAbstractesAImplementacionsGeneriques() throws Exception {
        // Comprova que el converter privat resol els tipus abstractes Dimensio i Fet a implementacions genèriques.
        MappingJackson2HttpMessageConverter converter = ReflectionTestUtils.invokeMethod(RestTemplateConfig.class, "getConverter");
        ObjectMapper objectMapper = converter.getObjectMapper();

        Object dimensio = objectMapper.readValue("{}", Dimensio.class);
        Object fet = objectMapper.readValue("{}", Fet.class);

        assertThat(dimensio).isInstanceOf(GenericDimensio.class);
        assertThat(fet).isInstanceOf(GenericFet.class);
    }
}
