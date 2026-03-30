package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalutSchedulerServiceTest {

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private SalutInfoHelper salutInfoHelper;

    @Mock
    private ParametresHelper parametresHelper;

    @Mock
    private TaskExecutor salutWorkerExecutor;

    private SalutSchedulerService schedulerService;
    private EntornApp entornApp;
    private EntornApp otherEntornApp;

    @BeforeEach
    void setUp() {
        schedulerService = new SalutSchedulerService(
                salutClientHelper,
                salutInfoHelper,
                parametresHelper,
                salutWorkerExecutor);
        ReflectionTestUtils.setField(schedulerService, "schedulerLeader", true);
        ReflectionTestUtils.setField(schedulerService, "schedulerBack", true);

        entornApp = sampleEntornApp(1L, "APP", "ENTORN");
        otherEntornApp = sampleEntornApp(2L, "APP2", "ENTORN2");
    }

    @Test
    void scheduledSalutTasks_quanNoEsLeader_noConsultaEntornsNiExecutaTasques() {
        ReflectionTestUtils.setField(schedulerService, "schedulerLeader", false);

        schedulerService.scheduledSalutTasks();

        verify(salutClientHelper, never()).entornAppFindByActivaTrue();
        verifyNoInteractions(salutInfoHelper, salutWorkerExecutor);
    }

    @Test
    void scheduledSalutTasks_quanNoHiHaEntornsActius_noExecutaCapWorker() {
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of());

        schedulerService.scheduledSalutTasks();

        verify(salutClientHelper).entornAppFindByActivaTrue();
        verifyNoInteractions(salutInfoHelper, salutWorkerExecutor);
    }

    @Test
    void scheduledSalutTasks_quanHiHaEntornsActius_executaUnaTascaPerCadaEntorn() {
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp, otherEntornApp));
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        schedulerService.scheduledSalutTasks();

        verify(salutInfoHelper).getSalutInfo(entornApp);
        verify(salutInfoHelper).getSalutInfo(otherEntornApp);
        verify(salutWorkerExecutor, times(2)).execute(any(Runnable.class));
    }

    @Test
    void scheduledSalutTasks_quanUnaExecucioJaEstaEnCurs_perLEntornIgnoraLaSegonaPlanificacio() {
        List<Runnable> queuedTasks = new ArrayList<>();
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp));
        doAnswer(invocation -> {
            queuedTasks.add(invocation.getArgument(0));
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        schedulerService.scheduledSalutTasks();
        schedulerService.scheduledSalutTasks();

        verify(salutWorkerExecutor, times(1)).execute(any(Runnable.class));
        verifyNoInteractions(salutInfoHelper);

        queuedTasks.get(0).run();

        verify(salutInfoHelper).getSalutInfo(entornApp);
    }

    @Test
    void scheduledSalutTasks_quanElWorkerRebutjaLaPrimeraExecucio_reiniciaElGuardIPermetReintentar() {
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp));
        doThrow(new TaskRejectedException("queue full"))
                .doAnswer(invocation -> {
                    Runnable runnable = invocation.getArgument(0);
                    runnable.run();
                    return null;
                })
                .when(salutWorkerExecutor).execute(any(Runnable.class));

        schedulerService.scheduledSalutTasks();
        schedulerService.scheduledSalutTasks();

        verify(salutInfoHelper, times(1)).getSalutInfo(eq(entornApp));
        verify(salutWorkerExecutor, times(2)).execute(any(Runnable.class));
    }

    @Test
    void scheduledSalutTasks_quanSalutInfoLlanzaExcepcio_noPropagaLErrorIContinua() {
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp, otherEntornApp));
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));
        doThrow(new IllegalStateException("boom")).when(salutInfoHelper).getSalutInfo(entornApp);

        assertThatCode(() -> schedulerService.scheduledSalutTasks())
                .doesNotThrowAnyException();

        verify(salutInfoHelper).getSalutInfo(entornApp);
        verify(salutInfoHelper).getSalutInfo(otherEntornApp);
    }

    @Test
    void informePeriodicSistema_quanNoEsLeader_noConsultaParametres() {
        ReflectionTestUtils.setField(schedulerService, "schedulerLeader", false);

        schedulerService.informePeriodicSistema();

        verifyNoInteractions(parametresHelper);
    }

    @Test
    void informePeriodicSistema_quanElReportEstaDesactivat_noGeneraMesAccions() {
        when(parametresHelper.getParametreBoolean(any(), eq(false))).thenReturn(false);

        schedulerService.informePeriodicSistema();

        verify(parametresHelper).getParametreBoolean(any(), eq(false));
    }

    @Test
    void informePeriodicSistema_quanElReportEstaActivatIFaServirThreadPool_noLlanzaExcepcio() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(3);
        executor.initialize();

        SalutSchedulerService service = new SalutSchedulerService(
                salutClientHelper,
                salutInfoHelper,
                parametresHelper,
                executor);
        ReflectionTestUtils.setField(service, "schedulerLeader", true);
        ReflectionTestUtils.setField(service, "schedulerBack", true);
        when(parametresHelper.getParametreBoolean(any(), eq(false))).thenReturn(true);

        assertThatCode(service::informePeriodicSistema).doesNotThrowAnyException();

        executor.shutdown();
    }

    private static EntornApp sampleEntornApp(Long id, String appNom, String entornNom) {
        return EntornApp.builder()
                .id(id)
                .app(new AppRef(id, appNom))
                .entorn(new EntornRef(id, entornNom))
                .salutUrl("http://localhost/" + appNom.toLowerCase())
                .activa(true)
                .build();
    }
}
