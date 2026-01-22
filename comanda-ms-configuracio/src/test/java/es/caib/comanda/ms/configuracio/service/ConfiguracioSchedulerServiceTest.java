package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.service.ConfiguracioSchedulerService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfiguracioSchedulerServiceTest {

    @Mock
    private EntornAppRepository entornAppRepository;
    @Mock
    private AppInfoHelper appInfoHelper;
    @Mock
    private TaskExecutor configuracioWorkerExecutor;

    private ConfiguracioSchedulerService service;
    private EntornAppEntity ea1;
    private EntornAppEntity ea2;

    @BeforeEach
    void setUp() {
        service = new ConfiguracioSchedulerService(
                entornAppRepository,
                appInfoHelper,
                configuracioWorkerExecutor
        );
        // Leader by default
        ReflectionTestUtils.setField(service, "schedulerLeader", true);
        ReflectionTestUtils.setField(service, "schedulerBack", true);

        AppEntity app1 = new AppEntity();
        app1.setId(1L);
        app1.setNom("App1");
        app1.setActiva(true);

        AppEntity app2 = new AppEntity();
        app2.setId(2L);
        app2.setNom("App2");
        app2.setActiva(true);

        EntornEntity entorn1 = new EntornEntity();
        entorn1.setId(1L);
        entorn1.setNom("Entorn1");

        EntornEntity entorn2 = new EntornEntity();
        entorn2.setId(2L);
        entorn2.setNom("Entorn2");

        ea1 = new EntornAppEntity();
        ea1.setId(10L);
        ea1.setApp(app1);
        ea1.setEntorn(entorn1);
        ea1.setActiva(true);

        ea2 = new EntornAppEntity();
        ea2.setId(20L);
        ea2.setApp(app2);
        ea2.setEntorn(entorn2);
        ea2.setActiva(true);
    }

    @Test
    void scheduledConfiguracioTasks_doesNotRun_whenNotLeader() {
        ReflectionTestUtils.setField(service, "schedulerLeader", false);

        service.scheduledConfiguracioTasks();

        verifyNoInteractions(entornAppRepository);
        verifyNoInteractions(appInfoHelper);
        verifyNoInteractions(configuracioWorkerExecutor);
    }

    @Test
    void scheduledConfiguracioTasks_noActiveEntornApps_doesNothing() {
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(List.of());

        service.scheduledConfiguracioTasks();

        verify(entornAppRepository).findByActivaTrueAndAppActivaTrue();
        verifyNoInteractions(appInfoHelper);
        verifyNoInteractions(configuracioWorkerExecutor);
    }

    @Test
    void scheduledConfiguracioTasks_executesForEachActiveEntornApp() {
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(List.of(ea1, ea2));
        // Inline runnable execution
        doAnswer(invocation -> { Runnable r = invocation.getArgument(0); r.run(); return null; })
                .when(configuracioWorkerExecutor).execute(any(Runnable.class));

        service.scheduledConfiguracioTasks();

        verify(appInfoHelper).refreshAppInfo(eq(10L));
        verify(appInfoHelper).refreshAppInfo(eq(20L));
        verify(appInfoHelper, times(2)).refreshAppInfo(any(Long.class));
    }

    @Test
    void scheduledConfiguracioTasks_continuesIfOneEntornAppThrows() {
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(List.of(ea1, ea2));
        doAnswer(invocation -> { Runnable r = invocation.getArgument(0); r.run(); return null; })
                .when(configuracioWorkerExecutor).execute(any(Runnable.class));

        doThrow(new RuntimeException("boom")).when(appInfoHelper).refreshAppInfo(eq(10L));

        assertDoesNotThrow(() -> service.scheduledConfiguracioTasks());

        verify(appInfoHelper).refreshAppInfo(eq(10L));
        verify(appInfoHelper).refreshAppInfo(eq(20L));
    }

    @Test
    void scheduledConfiguracioTasks_continuesIfTaskRejectedForOne() {
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(List.of(ea1, ea2));

        AtomicInteger call = new AtomicInteger();
        doAnswer(invocation -> {
            if (call.getAndIncrement() == 0) {
                throw new TaskRejectedException("queue full");
            }
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(configuracioWorkerExecutor).execute(any(Runnable.class));

        service.scheduledConfiguracioTasks();

        // First rejected -> no helper call for ea1; second runs
        verify(appInfoHelper, never()).refreshAppInfo(eq(10L));
        verify(appInfoHelper).refreshAppInfo(eq(20L));
    }
}
