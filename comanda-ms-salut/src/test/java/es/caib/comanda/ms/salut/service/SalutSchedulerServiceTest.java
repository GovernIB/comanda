package es.caib.comanda.ms.salut.service;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.service.SalutSchedulerService;
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
public class SalutSchedulerServiceTest {

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private SalutInfoHelper salutInfoHelper;

    @Mock
    private TaskExecutor salutWorkerExecutor;

    private SalutSchedulerService salutSchedulerService;
    private EntornApp entornApp;
    private EntornApp inactiveEntornApp;
    private EntornApp entornApp2;

    @BeforeEach
    void setUp() {
        // Create SalutSchedulerService instance
        salutSchedulerService = new SalutSchedulerService(
                salutClientHelper,
                salutInfoHelper,
                salutWorkerExecutor
        );

        // Set leader flags to true by default; tests may override
        ReflectionTestUtils.setField(salutSchedulerService, "schedulerLeader", true);
        ReflectionTestUtils.setField(salutSchedulerService, "schedulerBack", true);

        // Setup EntornApp
        entornApp = EntornApp.builder()
                .id(1L)
                .app(new AppRef(1L, "Test App"))
                .entorn(new EntornRef(1L, "Test Entorn"))
                .salutUrl("http://test.com/health")
                .activa(true)
                .build();

        entornApp2 = EntornApp.builder()
                .id(3L)
                .app(new AppRef(2L, "Test App 2"))
                .entorn(new EntornRef(2L, "Test Entorn 2"))
                .salutUrl("http://test2.com/health")
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

    @Test
    void scheduledSalutTasks_doesNotRun_whenNotLeader() {
        // Arrange: make instance not leader
        ReflectionTestUtils.setField(salutSchedulerService, "schedulerLeader", false);

        // Act
        salutSchedulerService.scheduledSalutTasks();

        // Assert: no calls to fetch entorn apps, no tasks executed
        verify(salutClientHelper, never()).entornAppFindByActivaTrue();
        verifyNoInteractions(salutInfoHelper);
        verifyNoInteractions(salutWorkerExecutor);
    }

    @Test
    void scheduledSalutTasks_noActiveEntornApps_doesNothing() {
        // Arrange
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of());

        // Act
        salutSchedulerService.scheduledSalutTasks();

        // Assert
        verify(salutClientHelper).entornAppFindByActivaTrue();
        verifyNoInteractions(salutInfoHelper);
        verifyNoInteractions(salutWorkerExecutor);
    }

    @Test
    void scheduledSalutTasks_executesForEachActiveEntornApp() {
        // Arrange
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp, entornApp2));
        // execute worker runnable inline
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        // Act
        salutSchedulerService.scheduledSalutTasks();

        // Assert
        verify(salutInfoHelper).getSalutInfo(eq(entornApp));
        verify(salutInfoHelper).getSalutInfo(eq(entornApp2));
        verify(salutInfoHelper, times(2)).getSalutInfo(any(EntornApp.class));
    }

    @Test
    void scheduledSalutTasks_continuesIfOneEntornAppThrows() {
        // Arrange
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp, entornApp2));
        doAnswer(invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        // Throw for the first, succeed for the second
        doThrow(new RuntimeException("boom")).when(salutInfoHelper).getSalutInfo(eq(entornApp));
        // entornApp2 default: no exception

        // Act & Assert: method should not throw even if one runnable fails internally
        assertDoesNotThrow(() -> salutSchedulerService.scheduledSalutTasks());

        // Verify both were attempted
        verify(salutInfoHelper).getSalutInfo(eq(entornApp));
        verify(salutInfoHelper).getSalutInfo(eq(entornApp2));
    }

    @Test
    void scheduledSalutTasks_continuesIfTaskRejectedForOne() {
        // Arrange
        when(salutClientHelper.entornAppFindByActivaTrue()).thenReturn(List.of(entornApp, entornApp2));

        AtomicInteger call = new AtomicInteger();
        doAnswer(invocation -> {
            if (call.getAndIncrement() == 0) {
                throw new TaskRejectedException("queue full");
            }
            Runnable r = invocation.getArgument(0);
            r.run();
            return null;
        }).when(salutWorkerExecutor).execute(any(Runnable.class));

        // Act
        salutSchedulerService.scheduledSalutTasks();

        // Assert: first was rejected so only second reaches helper
        verify(salutInfoHelper, never()).getSalutInfo(eq(entornApp));
        verify(salutInfoHelper).getSalutInfo(eq(entornApp2));
    }
}
