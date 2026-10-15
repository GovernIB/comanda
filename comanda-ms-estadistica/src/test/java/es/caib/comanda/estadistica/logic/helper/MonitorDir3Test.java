package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaris per a MonitorDir3")
class MonitorDir3Test {

    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private Authentication authentication;

    private static final String URL_TEST = "http://mock-dir3-url/api";
    private static final String USUARI_TEST = "test_user";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("El constructor ha d'inicialitzar els monitors amb l'usuari autenticat correctament")
    void constructor_AmbUsuariAutenticat_InicialitzaMonitors() {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Act
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        // Assert
        Monitor monitorInfo = monitorDir3.getMonitorInfo();
        assertNotNull(monitorInfo);
        assertNull(monitorInfo.getEntornAppId());
        assertEquals(ModulEnum.DIR3, monitorInfo.getModul());
        assertEquals(AccioTipusEnum.SORTIDA, monitorInfo.getTipus());
        assertEquals(URL_TEST, monitorInfo.getUrl());
        assertEquals("Obtenir unitats organitzatives", monitorInfo.getOperacio());
        assertEquals(USUARI_TEST, monitorInfo.getCodiUsuari());
        assertFalse(monitorDir3.isFinishedInfoAction());
        Monitor monitorOrganigrama = monitorDir3.getMonitorOrganigrama();
        assertNotNull(monitorOrganigrama);
        assertEquals("Obtenir organigrama d'unitats organitzatives", monitorOrganigrama.getOperacio());
        assertEquals(USUARI_TEST, monitorOrganigrama.getCodiUsuari());
        assertFalse(monitorDir3.isFinishedOrganigramaAction());
    }

    @Test
    @DisplayName("El constructor ha d'usar 'SCHEDULER' si l'autenticació és nul·la")
    void constructor_AmbAutenticacioNulla_UsaScheduler() {
        // Arrange & Act
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        // Assert
        assertEquals("SCHEDULER", monitorDir3.getMonitorInfo().getCodiUsuari());
        assertEquals("SCHEDULER", monitorDir3.getMonitorOrganigrama().getCodiUsuari());
    }

    @Test
    @DisplayName("El constructor ha d'usar 'SCHEDULER' si el nom d'usuari és buit")
    void constructor_AmbNomUsuariBuit_UsaScheduler() {
        // Arrange
        when(authentication.getName()).thenReturn("");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Act
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        // Assert
        assertEquals("SCHEDULER", monitorDir3.getMonitorInfo().getCodiUsuari());
    }

    @Test
    @DisplayName("startInfoAction i endInfoAction han de registrar l'èxit i el temps de resposta")
    void startIEndInfoAction_Exit_RegistraTempsIExecutaCreate() throws InterruptedException {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        // Act
        long abans = System.currentTimeMillis();
        monitorDir3.startInfoAction();
        Thread.sleep(15);
        monitorDir3.endInfoAction();
        long despres = System.currentTimeMillis();
        // Assert
        assertTrue(monitorDir3.isFinishedInfoAction());
        Monitor monitor = monitorDir3.getMonitorInfo();
        assertEquals(EstatEnum.OK, monitor.getEstat());
        assertNotNull(monitor.getData());
        long tempsResposta = monitor.getTempsResposta();
        assertTrue(tempsResposta >= 0, "El temps de resposta no pot ser negatiu");
        assertTrue(tempsResposta <= (despres - abans + 50), "El temps de resposta és coherent");
        verify(estadisticaClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("endInfoAction amb error ha de registrar l'excepció correctament")
    void endInfoAction_AmbError_RegistraExcepcio() {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        monitorDir3.startInfoAction();
        RuntimeException ex = new RuntimeException("Error de connexió a DIR3");
        // Act
        monitorDir3.endInfoAction(ex);
        // Assert
        Monitor monitor = monitorDir3.getMonitorInfo();
        assertEquals(EstatEnum.ERROR, monitor.getEstat());
        assertEquals("S'ha produït un error obtenint les unitats organitzatives", monitor.getErrorDescripcio());
        assertEquals("RuntimeException: Error de connexió a DIR3", monitor.getExcepcioMessage());
        assertNotNull(monitor.getExcepcioStacktrace());
        assertTrue(monitor.getExcepcioStacktrace().contains("java.lang.RuntimeException"));
        verify(estadisticaClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("startOrganigramaAction i endOrganigramaAction han de registrar l'èxit i el temps de resposta")
    void startIEndOrganigramaAction_Exit_RegistraTempsIExecutaCreate() throws InterruptedException {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        // Act
        long abans = System.currentTimeMillis();
        monitorDir3.startOrganigramaAction();
        Thread.sleep(15);
        monitorDir3.endOrganigramaAction();
        long despres = System.currentTimeMillis();
        // Assert
        assertTrue(monitorDir3.isFinishedOrganigramaAction());
        Monitor monitor = monitorDir3.getMonitorOrganigrama();
        assertEquals(EstatEnum.OK, monitor.getEstat());
        long tempsResposta = monitor.getTempsResposta();
        assertTrue(tempsResposta >= 0);
        assertTrue(tempsResposta <= (despres - abans + 50));
        verify(estadisticaClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("endOrganigramaAction amb error ha de registrar l'excepció correctament")
    void endOrganigramaAction_AmbError_RegistraExcepcio() {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorDir3 monitorDir3 = new MonitorDir3(URL_TEST, estadisticaClientHelper);
        monitorDir3.startOrganigramaAction();
        RuntimeException ex = new RuntimeException("Error parsejant organigrama");
        // Act
        monitorDir3.endOrganigramaAction(ex);
        // Assert
        Monitor monitor = monitorDir3.getMonitorOrganigrama();
        assertEquals(EstatEnum.ERROR, monitor.getEstat());
        assertEquals("S'ha produït un error obtenint l'organigrama de les unitats organitzatives", monitor.getErrorDescripcio());
        assertEquals("RuntimeException: Error parsejant organigrama", monitor.getExcepcioMessage());
        assertNotNull(monitor.getExcepcioStacktrace());
        verify(estadisticaClientHelper, times(1)).monitorCreate(monitor);
    }
}
