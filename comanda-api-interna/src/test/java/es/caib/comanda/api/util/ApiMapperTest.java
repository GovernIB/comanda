package es.caib.comanda.api.util;

import es.caib.comanda.model.v1.avis.Avis;
import es.caib.comanda.model.v1.tasca.Tasca;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApiMapperTest {

    private final ApiMapper mapper = Mappers.getMapper(ApiMapper.class);

    @Test
    @DisplayName("convert Tasca de client a model V1")
    void convertTasca_mapejaCamps() throws MalformedURLException {
        // Arrange
        es.caib.comanda.client.model.Tasca clientTasca = new es.caib.comanda.client.model.Tasca();
        clientTasca.setIdentificador("T1");
        clientTasca.setUrl(new URL("http://localhost"));
        clientTasca.setEstat("PENDENT");

        // Act
        Tasca apiTasca = mapper.convert(clientTasca);

        // Assert
        assertThat(apiTasca.getIdentificador()).isEqualTo("T1");
        assertThat(apiTasca.getRedireccio()).isEqualTo(new URL("http://localhost"));
    }

    @Test
    @DisplayName("convert Avis de client a model V1")
    void convertAvis_mapejaCamps() throws MalformedURLException {
        // Arrange
        es.caib.comanda.client.model.Avis clientAvis = new es.caib.comanda.client.model.Avis();
        clientAvis.setIdentificador("A1");
        clientAvis.setNom("Títol");

        // Act
        Avis apiAvis = mapper.convert(clientAvis);

        // Assert
        assertThat(apiAvis.getIdentificador()).isEqualTo("A1");
        assertThat(apiAvis.getNom()).isEqualTo("Títol");
    }

    @Test
    @DisplayName("toLocalDateTime converteix correctament")
    void toLocalDateTime_converteix() {
        OffsetDateTime now = OffsetDateTime.now();
        LocalDateTime local = mapper.toLocalDateTime(now);
        assertThat(local).isEqualTo(now.toLocalDateTime());
        assertThat(mapper.toLocalDateTime(null)).isNull();
    }

    @Test
    @DisplayName("toOffsetDateTime converteix correctament")
    void toOffsetDateTime_converteix() {
        LocalDateTime now = LocalDateTime.now();
        OffsetDateTime offset = mapper.toOffsetDateTime(now);
        assertThat(offset.toLocalDateTime()).isEqualTo(now);
        assertThat(mapper.toOffsetDateTime(null)).isNull();
    }
}
