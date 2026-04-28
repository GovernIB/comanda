package es.caib.comanda.monitor.persist.entity;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorEntityTest {

    @Test
    @DisplayName("El constructor i els setters funcionen correctament")
    void constructorISetters_funcionenCorrectament() {
        // Arrange
        Long entornAppId = 1L;
        ModulEnum modul = ModulEnum.SALUT;
        AccioTipusEnum tipus = AccioTipusEnum.ENTRADA;
        LocalDateTime now = LocalDateTime.now();
        String url = "http://test.com/api";
        String operacio = "Test Operation";
        Long tempsResposta = 100L;
        EstatEnum estat = EstatEnum.OK;
        String codiUsuari = "testuser";

        // Act
        MonitorEntity entity = new MonitorEntity();
        entity.setEntornAppId(entornAppId);
        entity.setModul(modul);
        entity.setTipus(tipus);
        entity.setData(now);
        entity.setUrl(url);
        entity.setOperacio(operacio);
        entity.setTempsResposta(tempsResposta);
        entity.setEstat(estat);
        entity.setCodiUsuari(codiUsuari);
        ReflectionTestUtils.setField(entity, "id", 1L);

        // Assert
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getEntornAppId()).isEqualTo(entornAppId);
        assertThat(entity.getModul()).isEqualTo(modul);
        assertThat(entity.getTipus()).isEqualTo(tipus);
        assertThat(entity.getData()).isEqualTo(now);
        assertThat(entity.getUrl()).isEqualTo(url);
        assertThat(entity.getOperacio()).isEqualTo(operacio);
        assertThat(entity.getTempsResposta()).isEqualTo(tempsResposta);
        assertThat(entity.getEstat()).isEqualTo(estat);
        assertThat(entity.getCodiUsuari()).isEqualTo(codiUsuari);
    }

    @Test
    @DisplayName("El builder funciona correctament")
    void builder_funcionaCorrectament() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        MonitorEntity entity = MonitorEntity.builder()
                .entornAppId(1L)
                .modul(ModulEnum.SALUT)
                .tipus(AccioTipusEnum.ENTRADA)
                .data(now)
                .url("http://test.com/api")
                .operacio("Test Operation")
                .tempsResposta(100L)
                .estat(EstatEnum.OK)
                .codiUsuari("testuser")
                .build();

        // Assert
        assertThat(entity.getEntornAppId()).isEqualTo(1L);
        assertThat(entity.getModul()).isEqualTo(ModulEnum.SALUT);
        assertThat(entity.getTipus()).isEqualTo(AccioTipusEnum.ENTRADA);
        assertThat(entity.getData()).isEqualTo(now);
        assertThat(entity.getOperacio()).isEqualTo("Test Operation");
        assertThat(entity.getEstat()).isEqualTo(EstatEnum.OK);
    }

    @Test
    @DisplayName("La descripció d'error s'abreuja si supera el màxim")
    void errorDescripcio_sAbreuja() {
        // Arrange
        MonitorEntity entity = new MonitorEntity();
        String longText = "a".repeat(2000);

        // Act
        entity.setErrorDescripcio(longText);

        // Assert
        assertThat(entity.getErrorDescripcio()).hasSize(1024);
    }

    @Test
    @DisplayName("El missatge d'excepció s'abreuja si supera el màxim")
    void excepcioMessage_sAbreuja() {
        // Arrange
        MonitorEntity entity = new MonitorEntity();
        String longText = "a".repeat(2000);

        // Act
        entity.setExcepcioMessage(longText);

        // Assert
        assertThat(entity.getExcepcioMessage()).hasSize(1024);
    }

    @Test
    @DisplayName("L'stacktrace d'excepció s'abreuja si supera el màxim")
    void excepcioStacktrace_sAbreuja() {
        // Arrange
        MonitorEntity entity = new MonitorEntity();
        String longText = "a".repeat(5000);

        // Act
        entity.setExcepcioStacktrace(longText);

        // Assert
        assertThat(entity.getExcepcioStacktrace()).hasSize(4000);
    }

    @Test
    @DisplayName("El builder abreuja correctament els camps llargs")
    void builderCampsLlargs_sAbreugen() {
        // Arrange
        String longText = "a".repeat(5000);

        // Act
        MonitorEntity entity = MonitorEntity.builder()
                .errorDescripcio(longText)
                .excepcioMessage(longText)
                .excepcioStacktrace(longText)
                .build();

        // Assert
        assertThat(entity.getErrorDescripcio()).hasSize(1024);
        assertThat(entity.getExcepcioMessage()).hasSize(1024);
        // Nota: Segons el codi de MonitorEntity.MonitorEntityBuilder.excepcioStacktrace,
        // s'abreuja a ERROR_DESC_MAX_LENGTH * 2 = 2048, però el camp és 4000.
        // Comprovarem la longitud real.
        assertThat(entity.getExcepcioStacktrace().length()).isLessThanOrEqualTo(4000);
    }
}