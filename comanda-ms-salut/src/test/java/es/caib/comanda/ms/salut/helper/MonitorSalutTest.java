package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.salut.logic.helper.MonitorSalut;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MonitorSalutTest {

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Captor
    private ArgumentCaptor<Monitor> monitorCaptor;

    private MonitorSalut monitorSalut;
    private Long entornAppId;
    private String salutUrl;

    @BeforeEach
    void setUp() {
        // Setup test data
        entornAppId = 1L;
        salutUrl = "http://test.com/health";

        // Mock SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");

        // Create MonitorSalut instance
        monitorSalut = new MonitorSalut(entornAppId, salutUrl, salutClientHelper);
    }

    @Test
    void testConstructor() {
        // Assert
        assertNotNull(monitorSalut);
        assertFalse(monitorSalut.isFinishedAction());
        assertNull(monitorSalut.getStartTime());
        assertNotNull(monitorSalut.getMonitor());
        assertEquals(salutClientHelper, monitorSalut.getSalutClientHelper());
    }

    @Test
    void testStartAction() {
        // Act
        monitorSalut.startAction();

        // Assert
        assertNotNull(monitorSalut.getStartTime());
        assertNotNull(monitorSalut.getMonitor().getData());
    }

    @Test
    void testEndAction_Success() {
        // Arrange
        monitorSalut.startAction();

        // Act
        monitorSalut.endAction();

        // Assert
        assertTrue(monitorSalut.isFinishedAction());
        verify(salutClientHelper).monitorCreate(monitorCaptor.capture());
        
        Monitor capturedMonitor = monitorCaptor.getValue();
        assertEquals(EstatEnum.OK, capturedMonitor.getEstat());
        assertNotNull(capturedMonitor.getTempsResposta());
        assertEquals(entornAppId, capturedMonitor.getEntornAppId());
        assertEquals(salutUrl, capturedMonitor.getUrl());
        assertEquals(ModulEnum.SALUT, capturedMonitor.getModul());
        assertEquals(AccioTipusEnum.SORTIDA, capturedMonitor.getTipus());
        assertEquals("testUser", capturedMonitor.getCodiUsuari());
    }

    @Test
    void testEndAction_Error() {
        // Arrange
        monitorSalut.startAction();
        Exception testException = new RuntimeException("Test exception");

        // Act
        monitorSalut.endAction(testException, null);

        // Assert
        verify(salutClientHelper).monitorCreate(monitorCaptor.capture());
        
        Monitor capturedMonitor = monitorCaptor.getValue();
        assertEquals(EstatEnum.ERROR, capturedMonitor.getEstat());
        assertNotNull(capturedMonitor.getTempsResposta());
        assertNotNull(capturedMonitor.getErrorDescripcio());
        assertNotNull(capturedMonitor.getExcepcioMessage());
        assertNotNull(capturedMonitor.getExcepcioStacktrace());
        assertTrue(capturedMonitor.getExcepcioMessage().contains("Test exception"));
    }

    @Test
    void testGetAuthenticatedUserCode_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        MonitorSalut monitorSalutNoAuth = new MonitorSalut(entornAppId, salutUrl, salutClientHelper);

        // Assert
        assertNotNull(monitorSalutNoAuth);
        assertEquals("SCHEDULER", monitorSalutNoAuth.getMonitor().getCodiUsuari());
    }

    @Test
    void testGetAuthenticatedUserCode_EmptyUsername() {
        // Arrange
        when(authentication.getName()).thenReturn("");

        // Act
        MonitorSalut monitorSalutEmptyUser = new MonitorSalut(entornAppId, salutUrl, salutClientHelper);

        // Assert
        assertNotNull(monitorSalutEmptyUser);
        assertEquals("SCHEDULER", monitorSalutEmptyUser.getMonitor().getCodiUsuari());
    }

    @Test
    void testGetAuthenticatedUserCode_Exception() {
        // Arrange
        when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Test exception"));

        // Act
        MonitorSalut monitorSalutException = new MonitorSalut(entornAppId, salutUrl, salutClientHelper);

        // Assert
        assertNotNull(monitorSalutException);
        assertEquals("SCHEDULER", monitorSalutException.getMonitor().getCodiUsuari());
    }
}