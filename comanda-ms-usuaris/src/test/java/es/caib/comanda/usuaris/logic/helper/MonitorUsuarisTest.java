package es.caib.comanda.usuaris.logic.helper;

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
@DisplayName("Tests unitaris per a MonitorUsuaris")
class MonitorUsuarisTest {

    @Mock private UsuarisClientHelper usuarisClientHelper;
    @Mock private Authentication authentication;

    private static final Long ENTORN_APP_ID = 99L;
    private static final String URL_TASCA = "http://mock-url/api";
    private static final String USUARI_TEST = "testUser";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("El constructor ha d'inicialitzar el monitor amb l'usuari autenticat correctament")
    void constructor_AmbUsuariAutenticat_InicialitzaMonitor() {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Act
        MonitorUsuaris monitorUsuaris = new MonitorUsuaris(ENTORN_APP_ID, URL_TASCA, usuarisClientHelper);
        // Assert
        Monitor monitor = monitorUsuaris.getMonitor();
        assertNotNull(monitor);
        assertEquals(ENTORN_APP_ID, monitor.getEntornAppId());
        assertEquals(ModulEnum.SALUT, monitor.getModul());
        assertEquals(AccioTipusEnum.SORTIDA, monitor.getTipus());
        assertEquals(URL_TASCA, monitor.getUrl());
        assertEquals("Obtenir dades d'un usuari", monitor.getOperacio());
        assertEquals(USUARI_TEST, monitor.getCodiUsuari());
        assertFalse(monitorUsuaris.isFinishedAction());
    }

    @Test
    @DisplayName("El constructor ha d'usar 'SCHEDULER' si l'autenticació és nul·la")
    void constructor_AmbAutenticacioNulla_UsaScheduler() {
        // Arrange & Act
        MonitorUsuaris monitorUsuaris = new MonitorUsuaris(ENTORN_APP_ID, URL_TASCA, usuarisClientHelper);
        // Assert
        assertEquals("SCHEDULER", monitorUsuaris.getMonitor().getCodiUsuari());
    }

    @Test
    @DisplayName("El constructor ha d'usar 'SCHEDULER' si el nom d'usuari és buit o nul")
    void constructor_AmbNomUsuariBuit_UsaScheduler() {
        // Arrange
        when(authentication.getName()).thenReturn("");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Act
        MonitorUsuaris monitorUsuaris = new MonitorUsuaris(ENTORN_APP_ID, URL_TASCA, usuarisClientHelper);
        // Assert
        assertEquals("SCHEDULER", monitorUsuaris.getMonitor().getCodiUsuari());
    }

    @Test
    @DisplayName("startAction i endAction han de registrar l'èxit i el temps de resposta")
    void startIEndAction_Exit_RegistraTempsIExecutaCreate() throws InterruptedException {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorUsuaris monitorUsuaris = new MonitorUsuaris(ENTORN_APP_ID, URL_TASCA, usuarisClientHelper);
        // Act
        long abans = System.currentTimeMillis();
        monitorUsuaris.startAction();
        Thread.sleep(10);
        monitorUsuaris.endAction();
        long despres = System.currentTimeMillis();
        // Assert
        Monitor monitor = monitorUsuaris.getMonitor();
        assertEquals(EstatEnum.OK, monitor.getEstat());
        assertNotNull(monitor.getData());
        long tempsResposta = monitor.getTempsResposta();
        assertTrue(tempsResposta >= 0, "El temps de resposta no pot ser negatiu");
        assertTrue(tempsResposta <= (despres - abans + 50), "El temps de resposta és coherent amb l'execució");
        assertTrue(monitorUsuaris.isFinishedAction());
        verify(usuarisClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("endAction amb error i descripció personalitzada ha de registrar l'error correctament")
    void endAction_AmbErrorIDescripcio_RegistraExcepcioIDescripcio() {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorUsuaris monitorUsuaris = new MonitorUsuaris(ENTORN_APP_ID, URL_TASCA, usuarisClientHelper);
        monitorUsuaris.startAction();
        RuntimeException ex = new RuntimeException("Error de connexió a la BBDD");
        String descripcioCustom = "No s'ha pogut connectar al servei d'usuaris";
        // Act
        monitorUsuaris.endAction(ex, descripcioCustom);
        // Assert
        Monitor monitor = monitorUsuaris.getMonitor();
        assertEquals(EstatEnum.ERROR, monitor.getEstat());
        assertEquals(descripcioCustom, monitor.getErrorDescripcio());
        assertEquals("RuntimeException: Error de connexió a la BBDD", monitor.getExcepcioMessage());
        assertNotNull(monitor.getExcepcioStacktrace());
        assertTrue(monitor.getExcepcioStacktrace().contains("java.lang.RuntimeException"));
        verify(usuarisClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("endAction amb error i descripció buida ha d'usar el missatge d'error per defecte")
    void endAction_AmbErrorIDescripcioBuida_UsaMissatgePerDefecte() {
        // Arrange
        when(authentication.getName()).thenReturn(USUARI_TEST);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonitorUsuaris monitorUsuaris = new MonitorUsuaris(ENTORN_APP_ID, URL_TASCA, usuarisClientHelper);
        monitorUsuaris.startAction();
        RuntimeException ex = new RuntimeException("Error genèric");
        // Act
        monitorUsuaris.endAction(ex, "   ");
        // Assert
        Monitor monitor = monitorUsuaris.getMonitor();
        assertEquals(EstatEnum.ERROR, monitor.getEstat());
        assertEquals("S'ha produït un error obtenint les dades de l'usuari'", monitor.getErrorDescripcio());
        assertEquals("RuntimeException: Error genèric", monitor.getExcepcioMessage());
        verify(usuarisClientHelper, times(1)).monitorCreate(monitor);
    }
}
