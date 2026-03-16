package es.caib.comanda.avisos.persist.entity;

import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.model.v1.avis.AvisTipus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class AvisEntityTest {

    @Test
    @DisplayName("El constructor AvisEntity(Avis) ha de copiar tots els camps correctament")
    void constructor_haDeCopiarCampsDeModel() {
        // Arrange
        Avis avis = new Avis();
        avis.setEntornAppId(1L);
        avis.setEntornId(2L);
        avis.setAppId(3L);
        avis.setIdentificador("ID-123");
        avis.setTipus(AvisTipus.ALERTA);
        avis.setNom("Nom avís");
        avis.setDescripcio("Descripció");
        avis.setResponsable("usuari1");
        avis.setUsuarisAmbPermis(Collections.singletonList("usuari2"));

        // Act
        AvisEntity entity = new AvisEntity(avis);

        // Assert
        assertThat(entity.getEntornAppId()).isEqualTo(1L);
        assertThat(entity.getIdentificador()).isEqualTo("ID-123");
        assertThat(entity.getTipus()).isEqualTo(AvisTipus.ALERTA);
        assertThat(entity.getNom()).isEqualTo("Nom avís");
        assertThat(entity.getResponsable()).isEqualTo("usuari1");
        assertThat(entity.getUsuarisAmbPermis()).containsExactly("usuari2");
    }

    @Test
    @DisplayName("AvisEntity ha de permetre assignar camps auditarles (simulat)")
    void entity_haDeTenirCampsBase() {
        // Arrange
        AvisEntity entity = new AvisEntity();
        
        // Act
        entity.setId(100L);
        
        // Assert
        assertThat(entity.getId()).isEqualTo(100L);
    }
}
