package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a MonitorUserInformation")
class MonitorUserInformationTest {

    @Mock
    private AlarmaClientHelper alarmaClientHelper;

    @Captor
    private ArgumentCaptor<Monitor> monitorCaptor;

    private static final String OPERACIO = "Test Operació";
    private static final String URL = "test-url";
    private static final String ERROR_PER_DEFECTE = "S'ha produït un error consultant la informació d'usuari";

    // ========================================================================
    // 1. TESTOS PER A CONSTRUCTOR I INICIALITZACIÓ
    // ========================================================================

    @Test
    @DisplayName("Constructor: inicialitza el monitor amb les dades i valors per defecte correctes")
    void constructor_quanEsCrida_llavorsInicialitzaMonitorCorrectament() {
        // Arrange & Act
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.endAction(); // Cridem endAction per capturar l'estat final del monitor

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        Monitor monitor = monitorCaptor.getValue();

        assertThat(monitor.getModul()).isEqualTo(ModulEnum.USUARIS);
        assertThat(monitor.getTipus()).isEqualTo(AccioTipusEnum.SORTIDA);
        assertThat(monitor.getUrl()).isEqualTo(URL);
        assertThat(monitor.getOperacio()).isEqualTo(OPERACIO);
        assertThat(monitor.getCodiUsuari()).isNull(); // No s'estableix en aquest constructor
    }

    // ========================================================================
    // 2. TESTOS PER A startAction
    // ========================================================================

    @Test
    @DisplayName("startAction: estableix la data i l'hora d'inici correctament")
    void startAction_quanEsCrida_llavorsEstableixDataIStartTime() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);

        // Act
        monitorInfo.startAction();

        // Assert
        // Verifiquem el camp privat startTime mitjançant reflexió
        Long startTime = (Long) ReflectionTestUtils.getField(monitorInfo, "startTime");
        assertThat(startTime).isNotNull();
        assertThat(startTime).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    // ========================================================================
    // 3. TESTOS PER A endAction (Èxit sense descripció)
    // ========================================================================

    @Test
    @DisplayName("endAction: registra estat OK, temps de resposta i crida al client")
    void endAction_quanEsCrida_llavorsRegistraOkITempsResposta() throws InterruptedException {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.startAction();
        Thread.sleep(15); // Assegurem una diferència de temps mesurable

        // Act
        monitorInfo.endAction();

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        Monitor monitor = monitorCaptor.getValue();

        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(15L);
        assertThat(monitor.getErrorDescripcio()).isNull();
    }

    @Test
    @DisplayName("endAction: gestiona correctament quan startAction no s'ha cridat prèviament")
    void endAction_quanStartActionNoCridat_llavorsCalculaTempsSenseExcepcions() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        // NO cridem startAction()

        // Act
        monitorInfo.endAction();

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        Monitor monitor = monitorCaptor.getValue();

        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.getData()).isNotNull(); // getElapsedTime() hauria d'haver-la inicialitzat com a fallback
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(0L);
    }

    // ========================================================================
    // 4. TESTOS PER A endAction(String) (Èxit amb descripció)
    // ========================================================================

    @Test
    @DisplayName("endAction(String): registra la descripció quan no és buida")
    void endActionAmbString_quanDescripcioNoEsBuida_llavorsRegistraDescripcio() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.startAction();
        String descripcio = "Avís: La consulta LDAP ha trigat més del normal";

        // Act
        monitorInfo.endAction(descripcio);

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isEqualTo(descripcio);
    }

    @Test
    @DisplayName("endAction(String): ignora la descripció quan és null")
    void endActionAmbString_quanDescripcioEsNull_llavorsNoRegistraDescripcio() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.startAction();

        // Act
        monitorInfo.endAction((String) null);

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isNull();
    }

    @Test
    @DisplayName("endAction(String): ignora la descripció quan és una cadena buida o amb espais")
    void endActionAmbString_quanDescripcioEsBuida_llavorsNoRegistraDescripcio() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.startAction();

        // Act
        monitorInfo.endAction("   ");

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isNull();
    }

    // ========================================================================
    // 5. TESTOS PER A endAction(Throwable, String) (Error)
    // ========================================================================

    @Test
    @DisplayName("endAction(Throwable, String): registra estat ERROR i la descripció personalitzada")
    void endActionAmbError_quanHiHaDescripcio_llavorsRegistraErrorIDescripcio() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.startAction();
        RuntimeException ex = new RuntimeException("Fallada de connexió LDAP");
        String customErrorMsg = "No s'ha pogut connectar amb el servidor d'usuaris";

        // Act
        monitorInfo.endAction(ex, customErrorMsg);

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        Monitor monitor = monitorCaptor.getValue();

        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(monitor.getErrorDescripcio()).isEqualTo(customErrorMsg);
        assertThat(monitor.getExcepcioMessage()).contains("Fallada de connexió LDAP");
        assertThat(monitor.getExcepcioStacktrace()).contains("java.lang.RuntimeException");
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("endAction(Throwable, String): usa el missatge per defecte quan la descripció és null o buida")
    void endActionAmbError_quanDescripcioEsBuida_llavorsUsaMissatgePerDefecte() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        monitorInfo.startAction();
        RuntimeException ex = new RuntimeException("Fallada interna");

        // Act & Assert (provar amb null)
        monitorInfo.endAction(ex, null);
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isEqualTo(ERROR_PER_DEFECTE);

        // Act & Assert (provar amb cadena buida/espais)
        monitorInfo.endAction(ex, "   ");
        verify(alarmaClientHelper, times(2)).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isEqualTo(ERROR_PER_DEFECTE);
    }

    @Test
    @DisplayName("endAction(Throwable, String): gestiona correctament quan startAction no s'ha cridat prèviament")
    void endActionAmbError_quanStartActionNoCridat_llavorsCalculaTempsSenseExcepcions() {
        // Arrange
        MonitorUserInformation monitorInfo = new MonitorUserInformation(OPERACIO, URL, alarmaClientHelper);
        RuntimeException ex = new RuntimeException("Fallada");
        // NO cridem startAction()

        // Act
        monitorInfo.endAction(ex, "Error sense start previ");

        // Assert
        verify(alarmaClientHelper, times(1)).monitorCreate(monitorCaptor.capture());
        Monitor monitor = monitorCaptor.getValue();

        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(monitor.getData()).isNotNull(); // Inicialitzat pel fallback de getElapsedTime
        assertThat(monitor.getTempsResposta()).isGreaterThanOrEqualTo(0L);
    }
}
