package es.caib.comanda.alarmes.persist.entity;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaUsuari;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AlarmaEntityTest {

    @Test
    @DisplayName("El constructor d'AlarmaEntity mapeja correctament des del model")
    void constructor_mapejaModelCorrectament() {
        // Arrange
        Alarma model = new Alarma();
        model.setEntornAppId(1L);
        model.setMissatge("Test missatge");
        model.setEstat(AlarmaEstat.ACTIVA);
        LocalDateTime ara = LocalDateTime.now();
        model.setDataActivacio(ara);

        AlarmaConfigEntity configEntity = new AlarmaConfigEntity();

        // Act
        AlarmaEntity entity = new AlarmaEntity(model, configEntity);

        // Assert
        assertThat(entity.getEntornAppId()).isEqualTo(1L);
        assertThat(entity.getMissatge()).isEqualTo("Test missatge");
        assertThat(entity.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        assertThat(entity.getDataActivacio()).isEqualTo(ara);
        assertThat(entity.getAlarmaConfig()).isEqualTo(configEntity);
    }

    @Test
    @DisplayName("El constructor d'AlarmaConfigEntity mapeja correctament des del model")
    void alarmaConfigConstructor_mapejaModelCorrectament() {
        // Arrange
        AlarmaConfig model = new AlarmaConfig();
        model.setEntornAppId(2L);
        model.setNom("Config 1");
        model.setMissatge("Missatge config");
        model.setAdmin(true);
        model.setCorreuGeneric(false);

        // Act
        AlarmaConfigEntity entity = new AlarmaConfigEntity(model);

        // Assert
        assertThat(entity.getEntornAppId()).isEqualTo(2L);
        assertThat(entity.getNom()).isEqualTo("Config 1");
        assertThat(entity.getMissatge()).isEqualTo("Missatge config");
        assertThat(entity.isAdmin()).isTrue();
        assertThat(entity.isCorreuGeneric()).isFalse();
    }

    @Test
    @DisplayName("El constructor d'AlarmaUsuariEntity mapeja correctament des del model")
    void alarmaUsuariConstructor_mapejaModelCorrectament() {
        // Arrange
        AlarmaUsuari model = new AlarmaUsuari();
        model.setUsuari("user1");
        model.setLlegida(true);

        AlarmaEntity alarmaEntity = new AlarmaEntity();

        // Act
        AlarmaUsuariEntity entity = new AlarmaUsuariEntity(model, alarmaEntity);

        // Assert
        assertThat(entity.getUsuari()).isEqualTo("user1");
        assertThat(entity.isLlegida()).isTrue();
        assertThat(entity.getAlarma()).isEqualTo(alarmaEntity);
    }
}
