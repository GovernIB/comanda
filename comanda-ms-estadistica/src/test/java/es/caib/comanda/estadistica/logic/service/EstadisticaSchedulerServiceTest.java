package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.estadistica.logic.helper.CompactacioHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstadisticaSchedulerServiceTest {

    @Mock
    private EstadisticaHelper estadisticaHelper;
    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private CompactacioHelper compactacioHelper;
    @Mock
    private ParametresHelper parametresHelper;
    @Mock
    private TaskExecutor estadisticaWorkerExecutor;

    private EstadisticaSchedulerService service;
    private EntornApp ea1;
    private EntornApp ea2;

    @BeforeEach
    void setUp() {
        service = new EstadisticaSchedulerService(
                estadisticaHelper,
                estadisticaClientHelper,
                compactacioHelper,
                parametresHelper,
                estadisticaWorkerExecutor
        );
        // Leader by default
        ReflectionTestUtils.setField(service, "schedulerLeader", true);
        ReflectionTestUtils.setField(service, "schedulerBack", true);

        LocalDateTime now = LocalDateTime.now();
        String cronNow = String.format("0 %d %d * * *", now.getMinute(), now.getHour());
        String cronOtherMinute = String.format("0 %d %d * * *", (now.getMinute() + 1) % 60, now.getHour());

        ea1 = EntornApp.builder()
                .id(1L)
                .app(new AppRef(1L, "App1"))
                .entorn(new EntornRef(1L, "Entorn1"))
                .activa(true)
                .estadisticaCron(cronNow)
                .compactable(true)
                .build();

        ea2 = EntornApp.builder()
                .id(2L)
                .app(new AppRef(2L, "App2"))
                .entorn(new EntornRef(2L, "Entorn2"))
                .activa(true)
                .estadisticaCron(cronOtherMinute)
                .compactable(true)
                .build();
    }

    @Test
    void scheduledEstadisticaTasks_doesNotRun_whenNotLeader() {
        ReflectionTestUtils.setField(service, "schedulerLeader", false);

        service.scheduledEstadisticaTasks();

        verifyNoInteractions(estadisticaClientHelper);
        verifyNoInteractions(estadisticaHelper);
        verifyNoInteractions(compactacioHelper);
        verifyNoInteractions(estadisticaWorkerExecutor);
    }

    @Test
    void scheduledEstadisticaTasks_onlyActiveEntornAppsFetched_andNoWorkWhenEmpty() {
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of());

        service.scheduledEstadisticaTasks();

        verify(estadisticaClientHelper).entornAppFindByActivaTrue();
        verifyNoInteractions(estadisticaHelper);
        verifyNoInteractions(estadisticaWorkerExecutor);
    }

    @Test
    void executarProces_runsOnlyWhenEstadisticaCronMatchesNow() {
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(ea1, ea2));
        doAnswer(invocation -> { Runnable r = invocation.getArgument(0); r.run(); return null; })
                .when(estadisticaWorkerExecutor).execute(any(Runnable.class));

        service.scheduledEstadisticaTasks();

        // ea1 matches now -> called; ea2 is next minute -> not called
        verify(estadisticaHelper).getEstadisticaInfoDades(eq(ea1));
        verify(estadisticaHelper, never()).getEstadisticaInfoDades(eq(ea2));
    }

    @Test
    void executarProces_notCalled_whenTimeDoesNotMatch() {
        // Set both to not match by shifting minute +2
        LocalDateTime now = LocalDateTime.now();
        String notNow = String.format("0 %d %d * * *", (now.getMinute() + 2) % 60, now.getHour());
        EntornApp ea = EntornApp.builder()
                .id(10L).app(new AppRef(10L, "A")).entorn(new EntornRef(10L, "E"))
                .activa(true).estadisticaCron(notNow).build();
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(ea));

        service.scheduledEstadisticaTasks();

        verifyNoInteractions(estadisticaHelper);
        verifyNoInteractions(estadisticaWorkerExecutor);
    }

    @Test
    void compactacio_runsOnlyWhenEnabledAndCronMatchesNow_andOnlyForCompactable() {
        // ea1 compactable true, ea2 compactable true but cron for stats not relevant here
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(ea1, ea2));
        doAnswer(invocation -> { Runnable r = invocation.getArgument(0); r.run(); return null; })
                .when(estadisticaWorkerExecutor).execute(any(Runnable.class));

        LocalDateTime now = LocalDateTime.now();
        String cronNow = String.format("0 %d %d * * *", now.getMinute(), now.getHour());
        when(parametresHelper.getParametreBoolean(BaseConfig.PROP_STATS_COMPACTAR_ACTIU, false)).thenReturn(true);
        when(parametresHelper.getParametreText(BaseConfig.PROP_STATS_COMPACTAR_CRON, "0 0 3 * * *")).thenReturn(cronNow);

        service.scheduledEstadisticaTasks();

        verify(compactacioHelper, atLeastOnce()).compactar(any(EntornApp.class));
        // both are compactable true by setup
        verify(compactacioHelper).compactar(eq(ea1));
        verify(compactacioHelper).compactar(eq(ea2));
    }

    @Test
    void compactacio_notRun_whenDisabled_orTimeDoesNotMatch() {
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(ea1));

        // Disabled
        when(parametresHelper.getParametreBoolean(BaseConfig.PROP_STATS_COMPACTAR_ACTIU, false)).thenReturn(false);
        service.scheduledEstadisticaTasks();
        verifyNoInteractions(compactacioHelper);

        // Enabled but wrong time
        reset(compactacioHelper);
        when(parametresHelper.getParametreBoolean(BaseConfig.PROP_STATS_COMPACTAR_ACTIU, false)).thenReturn(true);
        LocalDateTime now = LocalDateTime.now();
        String cronOther = String.format("0 %d %d * * *", (now.getMinute() + 5) % 60, now.getHour());
        when(parametresHelper.getParametreText(BaseConfig.PROP_STATS_COMPACTAR_CRON, "0 0 3 * * *")).thenReturn(cronOther);

        service.scheduledEstadisticaTasks();
        verifyNoInteractions(compactacioHelper);
    }

    @Test
    void continuesProcessing_ifOneEntornAppThrows_onEstadistica() {
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(ea1, ea2));
        doAnswer(invocation -> { Runnable r = invocation.getArgument(0); r.run(); return null; })
                .when(estadisticaWorkerExecutor).execute(any(Runnable.class));

        // Throw for the matching one
        doThrow(new RuntimeException("boom")).when(estadisticaHelper).getEstadisticaInfoDades(eq(ea1));

        assertDoesNotThrow(() -> service.scheduledEstadisticaTasks());

        // ea2 doesn't match now for stats, so only verify ea1 attempted
        verify(estadisticaHelper).getEstadisticaInfoDades(eq(ea1));
    }

    @Test
    void invalidCron_onOneEntornApp_doesNotStopOthers() {
        LocalDateTime now = LocalDateTime.now();
        String cronNow = String.format("0 %d %d * * *", now.getMinute(), now.getHour());
        EntornApp bad = EntornApp.builder()
                .id(100L).app(new AppRef(100L, "Bad")).entorn(new EntornRef(100L, "E"))
                .activa(true).estadisticaCron("bad cron").build();
        EntornApp good = EntornApp.builder()
                .id(101L).app(new AppRef(101L, "Good")).entorn(new EntornRef(101L, "E2"))
                .activa(true).estadisticaCron(cronNow).build();

        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(bad, good));
        doAnswer(invocation -> { Runnable r = invocation.getArgument(0); r.run(); return null; })
                .when(estadisticaWorkerExecutor).execute(any(Runnable.class));

        service.scheduledEstadisticaTasks();

        verify(estadisticaHelper).getEstadisticaInfoDades(eq(good));
        verify(estadisticaHelper, never()).getEstadisticaInfoDades(eq(bad));
    }

    @Test
    void continues_ifTaskRejected_forOneRunnable() {
        when(estadisticaClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(ea1, ea2));

        AtomicInteger call = new AtomicInteger();
        doAnswer(invocation -> {
            if (call.getAndIncrement() == 0) {
                throw new TaskRejectedException("queue full");
            }
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(estadisticaWorkerExecutor).execute(any(Runnable.class));

        service.scheduledEstadisticaTasks();

        // First submitted runnable (ea1) rejected -> no helper call for ea1; ea2 does not match now, so also no call
        verify(estadisticaHelper, never()).getEstadisticaInfoDades(any());
    }
}
