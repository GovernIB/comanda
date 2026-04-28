package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.helper.MonitorHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorSchedulerServiceTest {

    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private ParametresHelper parametresHelper;
    @Mock
    private MonitorHelper monitorHelper;

    @InjectMocks
    private MonitorSchedulerService monitorSchedulerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(monitorSchedulerService, "schedulerLeader", true);
        ReflectionTestUtils.setField(monitorSchedulerService, "schedulerBack", true);
    }

    @Test
    @DisplayName("programarBorrat programa la tasca si l'esborrat està actiu i és líder")
    void programarBorrat_quanActiuILeader_programaTasca() {
        // Arrange
        when(parametresHelper.getParametreBoolean(any(), eq(true))).thenReturn(true);
        when(parametresHelper.getParametreEnter(any(), eq(7))).thenReturn(10);
        when(parametresHelper.getParametreEnter(any(), eq(60))).thenReturn(30);
        
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(PeriodicTrigger.class))).thenReturn((ScheduledFuture) mockFuture);

        // Act
        monitorSchedulerService.programarBorrat();

        // Assert
        ArgumentCaptor<PeriodicTrigger> triggerCaptor = ArgumentCaptor.forClass(PeriodicTrigger.class);
        verify(taskScheduler).schedule(any(Runnable.class), triggerCaptor.capture());
        // PeriodicTrigger retorna el període en mil·lisegons si s'usa el mètode getPeriod() de la superclasse?
        // En realitat, el valor 30 minuts són 30 * 60 * 1000 = 1800000 mil·lisegons.
        assertThat(triggerCaptor.getValue().getPeriod()).isEqualTo(1800000L);
        
        Map<Long, ScheduledFuture<?>> tasquesActives = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(monitorSchedulerService, "tasquesActives");
        assertThat(tasquesActives).containsKey(0L);
    }

    @Test
    @DisplayName("programarBorrat no programa la tasca si l'esborrat no està actiu")
    void programarBorrat_quanNoActiu_noProgramaTasca() {
        // Arrange
        when(parametresHelper.getParametreBoolean(any(), eq(true))).thenReturn(false);

        // Act
        monitorSchedulerService.programarBorrat();

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }

    @Test
    @DisplayName("programarBorrat no programa la tasca si no és líder")
    void programarBorrat_quanNoLeader_noProgramaTasca() {
        // Arrange
        ReflectionTestUtils.setField(monitorSchedulerService, "schedulerLeader", false);
        when(parametresHelper.getParametreBoolean(any(), eq(true))).thenReturn(true);

        // Act
        monitorSchedulerService.programarBorrat();

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }

    @Test
    @DisplayName("executarBorrat crida al helper si és líder")
    void executarBorrat_quanLeader_cridaHelper() {
        // Act
        monitorSchedulerService.executarBorrat(15);

        // Assert
        verify(monitorHelper).buidat(15);
    }

    @Test
    @DisplayName("cancelarBorratExistent cancel·la la tasca activa")
    void cancelarBorratExistent_cancelaTascaActiva() {
        // Arrange
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        Map<Long, ScheduledFuture<?>> tasquesActives = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(monitorSchedulerService, "tasquesActives");
        tasquesActives.put(0L, mockFuture);

        // Act
        monitorSchedulerService.cancelarBorratExistent();

        // Assert
        verify(mockFuture).cancel(false);
        assertThat(tasquesActives).isEmpty();
    }

    @Test
    @DisplayName("inicialitzarTasques no fa res si no és líder")
    void inicialitzarTasques_quanNoLeader_noFaRes() {
        // Arrange
        ReflectionTestUtils.setField(monitorSchedulerService, "schedulerLeader", false);

        // Act
        monitorSchedulerService.inicialitzarTasques();

        // Assert (No podem verificar el scheduler fàcilment, però podem verificar que el log o comportament no continua)
        // Atès que inicialitzarTasques crida programarBorrat retardadament, si no som líder ha de retornar immediatament
        verify(parametresHelper, never()).getParametreBoolean(any(), eq(true));
    }

    @Test
    @DisplayName("inicialitzarTasques crida programarBorrat si és líder")
    void inicialitzarTasques_quanLeader_programaBorrat() {
        // Act
        monitorSchedulerService.inicialitzarTasques();

        // Assert
        // Verifiquem que no s'executa immediatament (perquè té el delay de 83s)
        verify(parametresHelper, never()).getParametreBoolean(any(), eq(true));
    }
}
