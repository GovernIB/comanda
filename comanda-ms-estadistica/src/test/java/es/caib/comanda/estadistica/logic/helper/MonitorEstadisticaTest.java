package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MonitorEstadisticaTest {

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    private MonitorEstadistica monitor;

    private static final Long ENTORN_APP_ID = 10L;
    private static final String INFO_URL = "http://test.com/stats/info";
    private static final String DADES_URL = "http://test.com/stats/dades";

    @BeforeEach
    void setUp() {
        monitor = new MonitorEstadistica(ENTORN_APP_ID, INFO_URL, DADES_URL, estadisticaClientHelper);
    }

    @Test
    @DisplayName("startInfoAction i endInfoAction: registra operació d'estructura i marca estat OK i finishedInfoAction=true")
    void endInfoAction_quanOK_registraMonitorCorrecte() {
        monitor.startInfoAction();
        assertThat(monitor.isStartedInfoAction()).isTrue();
        assertThat(monitor.isFinishedInfoAction()).isFalse();

        monitor.endInfoAction();

        assertThat(monitor.isFinishedInfoAction()).isTrue();
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(estadisticaClientHelper).monitorCreate(captor.capture());
        Monitor saved = captor.getValue();
        assertThat(saved.getEntornAppId()).isEqualTo(ENTORN_APP_ID);
        assertThat(saved.getModul()).isEqualTo(ModulEnum.ESTADISTICA);
        assertThat(saved.getTipus()).isEqualTo(AccioTipusEnum.SORTIDA);
        assertThat(saved.getUrl()).isEqualTo(INFO_URL);
        assertThat(saved.getOperacio()).isEqualTo("Obtenir informació de l'estructura de les estadístiques");
        assertThat(saved.getEstat()).isEqualTo(EstatEnum.OK);
    }

    @Test
    @DisplayName("endInfoAction amb error: marca finishedInfoAction=true i registra estat ERROR")
    void endInfoAction_quanError_marcaFinishedInfoActionTrue() {
        monitor.startInfoAction();
        RuntimeException ex = new RuntimeException("Error info test");

        monitor.endInfoAction(ex);

        assertThat(monitor.isFinishedInfoAction()).isTrue();
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(estadisticaClientHelper).monitorCreate(captor.capture());
        Monitor saved = captor.getValue();
        assertThat(saved.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(saved.getOperacio()).isEqualTo("Obtenir informació de l'estructura de les estadístiques");
        assertThat(saved.getErrorDescripcio()).isEqualTo("S'ha produït un error obtenint la informació de l'estructura de les estadístiques");
    }

    @Test
    @DisplayName("startDadesAction i endDadesAction: registra operació de dades i marca finishedDadesAction=true")
    void endDadesAction_quanOK_registraMonitorCorrecte() {
        monitor.startDadesAction();
        assertThat(monitor.isStartedDadesAction()).isTrue();
        assertThat(monitor.isFinishedDadesAction()).isFalse();

        monitor.endDadesAction();

        assertThat(monitor.isFinishedDadesAction()).isTrue();
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(estadisticaClientHelper).monitorCreate(captor.capture());
        Monitor saved = captor.getValue();
        assertThat(saved.getUrl()).isEqualTo(DADES_URL);
        assertThat(saved.getOperacio()).isEqualTo("Obtenir dades estadístiques");
        assertThat(saved.getEstat()).isEqualTo(EstatEnum.OK);
    }

    @Test
    @DisplayName("endDadesAction amb error: marca finishedDadesAction=true i registra estat ERROR")
    void endDadesAction_quanError_marcaFinishedDadesActionTrue() {
        monitor.startDadesAction();
        RuntimeException ex = new RuntimeException("Error dades test");

        monitor.endDadesAction(ex);

        assertThat(monitor.isFinishedDadesAction()).isTrue();
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(estadisticaClientHelper).monitorCreate(captor.capture());
        Monitor saved = captor.getValue();
        assertThat(saved.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(saved.getOperacio()).isEqualTo("Obtenir dades estadístiques");
        assertThat(saved.getErrorDescripcio()).isEqualTo("S'ha produït un error obtenint les dades estadístiques");
    }

    @Test
    @DisplayName("endCompactarAction: marca finishedCompactarAction=true tant en OK com en error")
    void endCompactarAction_marcaFinishedCompactarActionTrue() {
        monitor.startCompactarAction();
        assertThat(monitor.isStartedCompactarAction()).isTrue();
        assertThat(monitor.isFinishedCompactarAction()).isFalse();

        monitor.endCompactarAction();
        assertThat(monitor.isFinishedCompactarAction()).isTrue();

        monitor.startCompactarAction();
        monitor.endCompactarAction(new RuntimeException("Error compactar"));
        assertThat(monitor.isFinishedCompactarAction()).isTrue();
    }
}
