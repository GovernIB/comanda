package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a MonitorAlarmes")
class MonitorAlarmesTest {

    @Mock
    private AlarmaClientHelper alarmaClientHelper;

    private static final Long ENTORN_APP_ID = 100L;
    private static final String OPERACIO = "Test Operació";
    private static final String URL = "test@caib.es";
    private static final String CODI_USUARI = "usuari_test";

    @BeforeEach
    void setUp() {
        // No cal inicialitzar @InjectMocks perquè instanciem la classe manualment
        // per tenir un control total sobre els constructors.
    }

    // ========================================================================
    // 1. TESTOS PER A CONSTRUCTORS
    // ========================================================================

    @Test
    @DisplayName("Constructor amb 5 arguments: inicialitza amb AccioTipusEnum.SORTIDA per defecte")
    void constructor5Args_quanEsCrida_llavorsInicialitzaAmbTipusSortida() {
        // Arrange & Act
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );

        // Assert
        Monitor monitor = monitorAlarmes.getMonitor();
        assertThat(monitor).isNotNull();
        assertThat(monitor.getEntornAppId()).isEqualTo(ENTORN_APP_ID);
        assertThat(monitor.getModul()).isEqualTo(ModulEnum.ALARMES);
        assertThat(monitor.getTipus()).isEqualTo(AccioTipusEnum.SORTIDA);
        assertThat(monitor.getUrl()).isEqualTo(URL);
        assertThat(monitor.getOperacio()).isEqualTo(OPERACIO);
        assertThat(monitor.getCodiUsuari()).isEqualTo(CODI_USUARI);
    }

    @Test
    @DisplayName("Constructor amb 6 arguments: inicialitza amb el Tipus d'Acció especificat")
    void constructor6Args_quanEsCrida_llavorsInicialitzaAmbTipusPersonalitzat() {
        // Arrange & Act
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, AccioTipusEnum.ENTRADA, alarmaClientHelper
        );

        // Assert
        assertThat(monitorAlarmes.getMonitor().getTipus()).isEqualTo(AccioTipusEnum.ENTRADA);
    }

    // ========================================================================
    // 2. TESTOS PER A startAction
    // ========================================================================

    @Test
    @DisplayName("startAction: estableix la data i l'hora d'inici correctament")
    void startAction_quanEsCrida_llavorsEstableixDataIStartTime() {
        // Arrange
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );

        // Act
        monitorAlarmes.startAction();

        // Assert
        assertThat(monitorAlarmes.getMonitor().getData()).isNotNull();
        assertThat(monitorAlarmes.getMonitor().getData()).isBeforeOrEqualTo(LocalDateTime.now());

        // Verifiquem el camp privat startTime mitjançant reflexió
        Long startTime = (Long) ReflectionTestUtils.getField(monitorAlarmes, "startTime");
        assertThat(startTime).isNotNull();
        assertThat(startTime).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    // ========================================================================
    // 3. TESTOS PER A endAction (Èxit)
    // ========================================================================

    @Test
    @DisplayName("endAction: registra estat OK, temps de resposta i crida al client")
    void endAction_quanEsCrida_llavorsRegistraOkITempsResposta() throws InterruptedException {
        // Arrange
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );
        monitorAlarmes.startAction();
        Thread.sleep(10); // Assegurem que hi hagi una diferència de temps mesurable

        // Act
        monitorAlarmes.endAction();

        // Assert
        Monitor monitor = monitorAlarmes.getMonitor();
        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(10L);
        assertThat(monitor.getErrorDescripcio()).isNull();
        assertThat(monitor.getExcepcioMessage()).isNull();

        verify(alarmaClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("endAction: gestiona correctament quan startAction no s'ha cridat prèviament")
    void endAction_quanStartActionNoCridat_llavorsCalculaTempsSenseExcepcions() {
        // Arrange
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );
        // NO cridem startAction()

        // Act
        monitorAlarmes.endAction();

        // Assert
        Monitor monitor = monitorAlarmes.getMonitor();
        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.getData()).isNotNull(); // getElapsedTime() hauria d'haver-la inicialitzat
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(0L);

        verify(alarmaClientHelper, times(1)).monitorCreate(monitor);
    }

    // ========================================================================
    // 4. TESTOS PER A endAction (Error)
    // ========================================================================

    @Test
    @DisplayName("endAction amb error: registra estat ERROR i la descripció personalitzada")
    void endActionAmbError_quanHiHaDescripcio_llavorsRegistraErrorIDescripcio() {
        // Arrange
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );
        monitorAlarmes.startAction();
        RuntimeException ex = new RuntimeException("Error de connexió simulat");
        String customErrorMsg = "No s'ha pogut connectar amb el servidor de correu";

        // Act
        monitorAlarmes.endAction(ex, customErrorMsg);

        // Assert
        Monitor monitor = monitorAlarmes.getMonitor();
        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(monitor.getErrorDescripcio()).isEqualTo(customErrorMsg);
        assertThat(monitor.getExcepcioMessage()).contains("Error de connexió simulat");
        assertThat(monitor.getExcepcioStacktrace()).contains("java.lang.RuntimeException");
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(0L);

        verify(alarmaClientHelper, times(1)).monitorCreate(monitor);
    }

    @Test
    @DisplayName("endAction amb error: usa el missatge per defecte quan la descripció és buida o null")
    void endActionAmbError_quanDescripcioEsBuida_llavorsUsaMissatgePerDefecte() {
        // Arrange
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );
        monitorAlarmes.startAction();
        RuntimeException ex = new RuntimeException("Fallada interna");

        // Act & Assert (provar amb null)
        monitorAlarmes.endAction(ex, null);
        assertThat(monitorAlarmes.getMonitor().getErrorDescripcio())
            .isEqualTo("S'ha produït un error enviant el correu d'alarma");

        // Act & Assert (provar amb cadena buida/espais)
        monitorAlarmes.endAction(ex, "   ");
        assertThat(monitorAlarmes.getMonitor().getErrorDescripcio())
            .isEqualTo("S'ha produït un error enviant el correu d'alarma");

        verify(alarmaClientHelper, times(2)).monitorCreate(any(Monitor.class));
    }

    @Test
    @DisplayName("endAction amb error: gestiona correctament quan startAction no s'ha cridat prèviament")
    void endActionAmbError_quanStartActionNoCridat_llavorsCalculaTempsSenseExcepcions() {
        // Arrange
        MonitorAlarmes monitorAlarmes = new MonitorAlarmes(
            ENTORN_APP_ID, OPERACIO, URL, CODI_USUARI, alarmaClientHelper
        );
        RuntimeException ex = new RuntimeException("Fallada");
        // NO cridem startAction()

        // Act
        monitorAlarmes.endAction(ex, "Error sense start");

        // Assert
        Monitor monitor = monitorAlarmes.getMonitor();
        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(monitor.getData()).isNotNull(); // Inicialitzat pel fallback de getElapsedTime
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(0L);

        verify(alarmaClientHelper, times(1)).monitorCreate(monitor);
    }
}
