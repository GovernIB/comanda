package es.caib.comanda.ms.salut.service;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.service.SalutSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalutSchedulerServiceTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private SalutInfoHelper salutInfoHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    @Mock
    private TaskExecutor salutWorkerExecutor;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    private ArgumentCaptor<PeriodicTrigger> triggerCaptor;

    private SalutSchedulerService salutSchedulerService;
    private EntornApp entornApp;
    private EntornApp inactiveEntornApp;

    @BeforeEach
    void setUp() {
        // Create SalutSchedulerService instance
        salutSchedulerService = new SalutSchedulerService(
                taskScheduler,
                salutClientHelper,
                salutInfoHelper,
                httpAuthorizationHeaderHelper,
                salutWorkerExecutor
        );

        // Set schedulerLeader to true for testing
        ReflectionTestUtils.setField(salutSchedulerService, "schedulerLeader", true);
        ReflectionTestUtils.setField(salutSchedulerService, "schedulerBack", true);

		// Set schedulerBack to true for testing
		ReflectionTestUtils.setField(salutSchedulerService, "schedulerBack", true);

        // Setup EntornApp
        entornApp = EntornApp.builder()
                .id(1L)
                .app(new AppRef(1L, "Test App"))
                .entorn(new EntornRef(1L, "Test Entorn"))
                .salutUrl("http://test.com/health")
                .activa(true)
                .build();

        inactiveEntornApp = EntornApp.builder()
                .id(2L)
                .app(new AppRef(1L, "Test App"))
                .entorn(new EntornRef(1L, "Test Entorn"))
                .salutUrl("http://test.com/health")
                .activa(false)
                .build();

    }

//    @Test
//    void testInicialitzarTasques() {
//        // Arrange
//        List<EntornApp> entornApps = Arrays.asList(entornApp);
//        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(entornApps);
//        // Mock taskScheduler.schedule to return scheduledFuture
//        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
//
//        // Act
//        salutSchedulerService.inicialitzarTasques();
//
//        // Assert
//        verify(salutClientHelper).entornAppFindByActivaTrue();
//        verify(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
//    }

    @Test
    void testProgramarTasca_ActiveEntornApp() {
        Map<Long, ScheduledFuture<?>> tasquesActives = new HashMap<>();
        ReflectionTestUtils.setField(salutSchedulerService, "tasquesActives", tasquesActives);

        // Act
        salutSchedulerService.programarTasca(entornApp);
        // Mock taskScheduler.schedule to return scheduledFuture
//        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));

        // Assert
        verify(taskScheduler).schedule(runnableCaptor.capture(), triggerCaptor.capture());

        PeriodicTrigger trigger = triggerCaptor.getValue();
//        assertEquals(15 * 60 * 1000, trigger.getPeriod());

        // Verify the task is stored in tasquesActives
//        Map<Long, ScheduledFuture<?>> tasquesActives = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(salutSchedulerService, "tasquesActives");
        assertNotNull(tasquesActives);
        assertTrue(tasquesActives.containsKey(1L));
    }

    @Test
    void testProgramarTasca_InactiveEntornApp() {
        // Act
        salutSchedulerService.programarTasca(inactiveEntornApp);

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(PeriodicTrigger.class));

        // Verify the task is not stored in tasquesActives
        Map<Long, ScheduledFuture<?>> tasquesActives = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(salutSchedulerService, "tasquesActives");
        assertNotNull(tasquesActives);
        assertFalse(tasquesActives.containsKey(2L));
    }

    @Test
    void testCancelarTascaExistent_TaskExists() {
        // Arrange
        Map<Long, ScheduledFuture<?>> tasquesActives = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(salutSchedulerService, "tasquesActives");
        tasquesActives.put(1L, scheduledFuture);

        // Act
        salutSchedulerService.cancelarTascaExistent(1L);

        // Assert
        verify(scheduledFuture).cancel(eq(false));
        assertFalse(tasquesActives.containsKey(1L));
    }

    @Test
    void testCancelarTascaExistent_TaskDoesNotExist() {
        // Act
        salutSchedulerService.cancelarTascaExistent(1L);

        // Assert
        verify(scheduledFuture, never()).cancel(anyBoolean());
    }

    @Test
    void testExecutarProces() {
        // Arrange
        Map<Long, ScheduledFuture<?>> tasquesActives = mock(Map.class);
        ReflectionTestUtils.setField(salutSchedulerService, "tasquesActives", tasquesActives);
        // First schedule a task to capture the Runnable
        salutSchedulerService.programarTasca(entornApp);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(PeriodicTrigger.class));
        // Make the worker executor run the submitted runnable synchronously
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        // Act
        // Execute the captured Runnable (which will dispatch to the worker executor)
        runnableCaptor.getValue().run();

        // Assert: the worker has executed and the helper has been called
        verify(salutInfoHelper).getSalutInfo(eq(entornApp));
    }

    @Test
    void testExecutarProces_Exception() {
        // Arrange
        Map<Long, ScheduledFuture<?>> tasquesActives = mock(Map.class);
        ReflectionTestUtils.setField(salutSchedulerService, "tasquesActives", tasquesActives);
        // First schedule a task to capture the Runnable
        salutSchedulerService.programarTasca(entornApp);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(PeriodicTrigger.class));

        // Make the worker executor run the submitted runnable synchronously
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        // Mock salutInfoHelper to throw exception
        doThrow(new RuntimeException("Test exception")).when(salutInfoHelper).getSalutInfo(any(EntornApp.class));

        // Act & Assert - should not throw exception (worker catches exceptions)
        assertDoesNotThrow(() -> runnableCaptor.getValue().run());
        verify(salutInfoHelper).getSalutInfo(eq(entornApp));
    }

    @Test
    void testComprovarRefrescInfo() {
        // Arrange
        List<EntornApp> entornApps = Arrays.asList(entornApp);
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(entornApps);
        // Mock taskScheduler.schedule to return scheduledFuture
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));

        // Act
        salutSchedulerService.comprovarRefrescInfo();

        // Assert
        verify(salutClientHelper).entornAppFindByActivaTrue();
        // Since the task doesn't exist in tasquesActives, it should be scheduled
        verify(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }

    @Test
    void testComprovarRefrescInfo_TaskAlreadyExists() {
        // Arrange
        List<EntornApp> entornApps = Arrays.asList(entornApp);
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(entornApps);

        // Add the task to tasquesActives
        Map<Long, ScheduledFuture<?>> tasquesActives = (Map<Long, ScheduledFuture<?>>) ReflectionTestUtils.getField(salutSchedulerService, "tasquesActives");
        tasquesActives.put(1L, scheduledFuture);

        // Act
        salutSchedulerService.comprovarRefrescInfo();

        // Assert
        verify(salutClientHelper).entornAppFindByActivaTrue();
        // Since the task already exists in tasquesActives and interval has not changed, it should not be scheduled again
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }

}
