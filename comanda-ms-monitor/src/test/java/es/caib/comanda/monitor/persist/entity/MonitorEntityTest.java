package es.caib.comanda.monitor.persist.entity;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MonitorEntityTest {

    @Test
    void testEntityCreation() {
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
        ReflectionTestUtils.setField(entity, "id", 1L);

        // Assert
        assertEquals(1L, entity.getId().longValue());
        assertEquals(1L, entity.getEntornAppId().longValue());
        assertEquals(ModulEnum.SALUT, entity.getModul());
        assertEquals(AccioTipusEnum.ENTRADA, entity.getTipus());
        assertEquals(now, entity.getData());
        assertEquals("http://test.com/api", entity.getUrl());
        assertEquals("Test Operation", entity.getOperacio());
        assertEquals(100L, entity.getTempsResposta().longValue());
        assertEquals(EstatEnum.OK, entity.getEstat());
        assertEquals("testuser", entity.getCodiUsuari());
    }

    @Test
    void testErrorDescripcioTruncation() {
        // Arrange
        MonitorEntity entity = new MonitorEntity();
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longText.append("a");
        }
        
        // Act
        entity.setErrorDescripcio(longText.toString());
        
        // Assert
        assertTrue(entity.getErrorDescripcio().length() <= 1024);
    }

    @Test
    void testExcepcioMessageTruncation() {
        // Arrange
        MonitorEntity entity = new MonitorEntity();
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longText.append("a");
        }
        
        // Act
        entity.setExcepcioMessage(longText.toString());
        
        // Assert
        assertTrue(entity.getExcepcioMessage().length() <= 1024);
    }

    @Test
    void testExcepcioStacktraceTruncation() {
        // Arrange
        MonitorEntity entity = new MonitorEntity();
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            longText.append("a");
        }
        
        // Act
        entity.setExcepcioStacktrace(longText.toString());
        
        // Assert
        assertTrue(entity.getExcepcioStacktrace().length() <= 4000);
    }

    @Test
    void testBuilderErrorDescripcioTruncation() {
        // Arrange
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longText.append("a");
        }
        
        // Act
        MonitorEntity entity = MonitorEntity.builder()
                .errorDescripcio(longText.toString())
                .build();
        
        // Assert
        assertTrue(entity.getErrorDescripcio().length() <= 1024);
    }

    @Test
    void testBuilderExcepcioMessageTruncation() {
        // Arrange
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            longText.append("a");
        }
        
        // Act
        MonitorEntity entity = MonitorEntity.builder()
                .excepcioMessage(longText.toString())
                .build();
        
        // Assert
        assertTrue(entity.getExcepcioMessage().length() <= 1024);
    }

    @Test
    void testBuilderExcepcioStacktraceTruncation() {
        // Arrange
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            longText.append("a");
        }
        
        // Act
        MonitorEntity entity = MonitorEntity.builder()
                .excepcioStacktrace(longText.toString())
                .build();
        
        // Assert
        assertTrue(entity.getExcepcioStacktrace().length() <= 4000);
    }
}