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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfiguracioSchedulerServiceTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private EntornAppRepository entornAppRepository;

    @Mock
    private AppInfoHelper appInfoHelper;

    @Mock
    private TaskExecutor configuracioWorkerExecutor;

    private ConfiguracioSchedulerService schedulerService;

    private EntornAppEntity entornAppEntity;
    private List<EntornAppEntity> activeEntornApps;
    private ScheduledFuture<?> scheduledFuture;

    @BeforeEach
    void setUp() {
        // Create the scheduler service with mocked dependencies
        schedulerService = new ConfiguracioSchedulerService(taskScheduler, entornAppRepository, appInfoHelper, configuracioWorkerExecutor);

        // Set the schedulerLeader property to true for testing
        ReflectionTestUtils.setField(schedulerService, "schedulerLeader", true);

		// Set the schedulerBack property to true for testing
		ReflectionTestUtils.setField(schedulerService, "schedulerBack", true);

        // Setup test data
        AppEntity appEntity = new AppEntity();
        appEntity.setId(1L);
        appEntity.setNom("Test App");
        appEntity.setActiva(true);

        EntornEntity entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");

        entornAppEntity = new EntornAppEntity();
        entornAppEntity.setId(1L);
        entornAppEntity.setApp(appEntity);
        entornAppEntity.setEntorn(entornEntity);
        entornAppEntity.setInfoUrl("http://test.com/info");
        entornAppEntity.setActiva(true);

        activeEntornApps = new ArrayList<>();
        activeEntornApps.add(entornAppEntity);

        // Create a mock for ScheduledFuture
        scheduledFuture = mock(ScheduledFuture.class);
    }

//    @Test
//    void testInicialitzarTasques() {
//        // Mock the repository to return active EntornApps
//        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(activeEntornApps);
//        // Mock the taskScheduler to return the mock ScheduledFuture
//        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
//
//        // Call the method to test
//        schedulerService.inicialitzarTasques();
//
//        // Verify that the repository was called
//        verify(entornAppRepository).findByActivaTrueAndAppActivaTrue();
//
//        // Verify that the taskScheduler was called to schedule a task
//        verify(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
//        // Verify that programarTasca was called for each active EntornAppEntity
//        //for (EntornAppEntity entity : activeEntornApps) {
//        //verify(taskScheduler).schedule(argThat(runnable -> {
//        //    runnable.run();
//        //    return true;
//        //}), any(PeriodicTrigger.class));
//    }

    @Test
    void testProgramarTasca() {
        // Mock the taskScheduler to return the mock ScheduledFuture
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));

        // Call the method to test
        schedulerService.programarTasca(entornAppEntity);

        // Verify that the taskScheduler was called to schedule a task
        ArgumentCaptor<PeriodicTrigger> triggerCaptor = ArgumentCaptor.forClass(PeriodicTrigger.class);
        verify(taskScheduler).schedule(any(Runnable.class), triggerCaptor.capture());

        // Verify that the trigger has the correct period
        PeriodicTrigger trigger = triggerCaptor.getValue();
        assertEquals(TimeUnit.MINUTES.toMillis(10), trigger.getPeriod());
    }

    @Test
    void testProgramarTascaWithInactiveEntornApp() {
        // Set the EntornApp to inactive
        entornAppEntity.setActiva(false);

        // Call the method to test
        schedulerService.programarTasca(entornAppEntity);

        // Verify that the taskScheduler was not called
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }

    @Test
    void testCancelarTascaExistent() {
        // Setup the tasquesActives map with a scheduled task
        @SuppressWarnings("unchecked")
        Map<Long, ScheduledFuture<?>> tasquesActives = mock(Map.class);
        doReturn(scheduledFuture).when(tasquesActives).get(1L);
        ReflectionTestUtils.setField(schedulerService, "tasquesActives", tasquesActives);

        // Call the method to test
        schedulerService.cancelarTascaExistent(1L);

        // Verify that the scheduled task was canceled
        verify(scheduledFuture).cancel(false);
        verify(tasquesActives).remove(1L);
    }

    @Test
    void testComprovarRefrescInfo() {
        // Mock the repository to return active EntornApps
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(activeEntornApps);

        // Setup the tasquesActives map to return null for the EntornApp ID
        @SuppressWarnings("unchecked")
        Map<Long, ScheduledFuture<?>> tasquesActives = mock(Map.class);
        doReturn(null).when(tasquesActives).get(1L);
        ReflectionTestUtils.setField(schedulerService, "tasquesActives", tasquesActives);

        // Call the method to test
        schedulerService.comprovarRefrescInfo();

        // Verify that the repository was called
        verify(entornAppRepository).findByActivaTrueAndAppActivaTrue();

        // Verify that programarTasca was called for the EntornApp
        // This is a bit tricky because we're testing a private method indirectly
        verify(taskScheduler).schedule(any(Runnable.class), any(PeriodicTrigger.class));
    }
}
