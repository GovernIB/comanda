package es.caib.comanda.tasques.persist.entity;

import es.caib.comanda.model.v1.tasca.TascaEstat;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class TascaEntityTest {

    @Test
    void entity_ambTascaModel_mapejaCampsCorrectament() throws MalformedURLException {
        // Arrange
        Tasca tasca = new Tasca();
        tasca.setEntornAppId(1L);
        tasca.setEntornId(2L);
        tasca.setAppId(3L);
        tasca.setIdentificador("ID-123");
        tasca.setNom("Nom Tasca");
        tasca.setTipus("TIPUS");
        tasca.setEstat(TascaEstat.PENDENT);
        tasca.setUrl(new URL("http://localhost"));
        tasca.setDataInici(LocalDateTime.now());
        tasca.setUsuarisAmbPermis(Collections.singletonList("usuari1"));

        // Act
        TascaEntity entity = new TascaEntity();
        entity.setEntornAppId(tasca.getEntornAppId());
        entity.setEntornId(tasca.getEntornId());
        entity.setAppId(tasca.getAppId());
        entity.setIdentificador(tasca.getIdentificador());
        entity.setNom(tasca.getNom());
        entity.setTipus(tasca.getTipus());
        entity.setEstat(tasca.getEstat());
        entity.setUrl(tasca.getUrl());
        entity.setDataInici(tasca.getDataInici());
        entity.setUsuarisAmbPermis(tasca.getUsuarisAmbPermis());

        // Assert
        assertThat(entity.getEntornAppId()).isEqualTo(1L);
        assertThat(entity.getEntornId()).isEqualTo(2L);
        assertThat(entity.getAppId()).isEqualTo(3L);
        assertThat(entity.getIdentificador()).isEqualTo("ID-123");
        assertThat(entity.getNom()).isEqualTo("Nom Tasca");
        assertThat(entity.getEstat()).isEqualTo(TascaEstat.PENDENT);
        assertThat(entity.getUrl().toString()).isEqualTo("http://localhost");
        assertThat(entity.getUsuarisAmbPermis()).containsExactly("usuari1");
    }

    @Test
    void setAndGet_funcionenCorrectament() {
        // Arrange
        TascaEntity entity = new TascaEntity();
        
        // Act
        entity.setDescripcio("Descripcio");
        entity.setEstatDescripcio("Estat desc");

        // Assert
        assertThat(entity.getDescripcio()).isEqualTo("Descripcio");
        assertThat(entity.getEstatDescripcio()).isEqualTo("Estat desc");
    }
}
