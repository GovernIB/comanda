package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.helper.DatasourcePoolMetricsHelper;
import es.caib.comanda.monitor.logic.helper.DatasourcePoolMetricsHelper.RawDatasourcePoolSample;
import es.caib.comanda.monitor.logic.helper.JmsQueueMetricsHelper;
import es.caib.comanda.monitor.logic.helper.JmsQueueMetricsHelper.RawJmsQueueSample;
import es.caib.comanda.monitor.logic.helper.WorkerPoolMetricsHelper;
import es.caib.comanda.monitor.logic.helper.WorkerPoolMetricsHelper.RawWorkerPoolSample;
import es.caib.comanda.monitor.logic.intf.model.performance.DatasourcePoolMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.DatasourcePoolSampleDto;
import es.caib.comanda.monitor.logic.intf.model.performance.JmsQueueMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.PerformanceMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WaitCountType;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolType;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a PerformanceMetricsServiceImpl")
class PerformanceMetricsServiceImplTest {

    @Mock
    private DatasourcePoolMetricsHelper helper;

    @Mock
    private WorkerPoolMetricsHelper workerHelper;

    @Mock
    private JmsQueueMetricsHelper jmsHelper;

    @Mock
    private ParametresHelper parametresHelper;

    @InjectMocks
    private PerformanceMetricsServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(parametresHelper.getParametreBoolean(BaseConfig.PROP_MONITOR_RENDIMENT_ACTIU, true))
                .thenReturn(true);
    }

    @Test
    @DisplayName("JBoss INTERVAL_DELTA: calcula deltes per interval, gestiona reinicis i getMetrics() no altera l'estat")
    void collectMetrics_jbossDelta_calculaDeltasIGestionaReinicis() {
        when(helper.collectAll()).thenReturn(
                List.of(jbossSample("comandaDB", true, 5, 50, 2, 10, 100L, 12L)),
                List.of(jbossSample("comandaDB", true, 5, 50, 8, 12, 107L, 45L)),
                List.of(jbossSample("comandaDB", true, 5, 50, 3, 12, 107L, 45L)),
                List.of(jbossSample("comandaDB", true, 5, 50, 4, 12, 3L, 45L))
        );

        LocalDateTime base = LocalDateTime.of(2026, 11, 19, 10, 0, 0);

        // Tick 1 (baseline -> delta = 0)
        service.collectMetricsAt(base);
        PerformanceMetricsDto snap1 = service.getMetrics();
        PerformanceMetricsDto snap1Repeat = service.getMetrics();
        assertThat(snap1.getDatasources().get(0).getWaitCount()).isZero();
        assertThat(snap1Repeat.getDatasources().get(0).getWaitCount()).isZero();
        assertThat(snap1.getDatasources().get(0).getHistory()).hasSize(1);
        assertThat(snap1.getSamplingIntervalSeconds()).isEqualTo(1);

        // Tick 2 (107 - 100 = 7)
        service.collectMetricsAt(base.plusSeconds(1));
        DatasourcePoolMetricsDto ds2 = service.getMetrics().getDatasources().get(0);
        assertThat(ds2.getWaitCount()).isEqualTo(7L);
        assertThat(ds2.getMaxWaitTimeMs()).isEqualTo(45L);
        assertThat(ds2.getHistory()).hasSize(2);
        assertThat(ds2.getHistory().get(1).getWaitCount()).isEqualTo(7L);

        // Tick 3 (sense canvi -> 0)
        service.collectMetricsAt(base.plusSeconds(2));
        assertThat(service.getMetrics().getDatasources().get(0).getWaitCount()).isZero();

        // Tick 4 (reinici comptador JBoss -> 3)
        service.collectMetricsAt(base.plusSeconds(3));
        assertThat(service.getMetrics().getDatasources().get(0).getWaitCount()).isEqualTo(3L);
        assertThat(service.getMetrics().getDatasources().get(0).getHistory()).hasSize(4);

        verify(helper, times(4)).collectAll();
    }

    @Test
    @DisplayName("HikariCP INSTANTANEOUS: conserva el valor puntual i agrega per MAX en fer downsampling")
    void collectMetrics_hikariInstantaneous_conservaMaxEnBuckets() {
        AtomicInteger sec = new AtomicInteger(0);
        when(helper.collectAll()).thenAnswer(inv -> {
            int i = sec.getAndIncrement();
            long wait = (i == 42) ? 6L : 0L;
            return List.of(hikariSample("HikariPool-1", wait, 18L));
        });

        LocalDateTime start = LocalDateTime.of(2026, 11, 19, 10, 0, 0);
        for (int i = 0; i < 300; i++) {
            service.collectMetricsAt(start.plusSeconds(i));
        }

        // Vista per defecte 5m sense agregació forçada (300 pts / 180 maxPoints -> bucket 2s)
        PerformanceMetricsDto res = service.getMetrics(300, null, null, 180);
        assertThat(res.getBucketSeconds()).isEqualTo(2);
        DatasourcePoolMetricsDto ds = res.getDatasources().get(0);
        assertThat(ds.getWaitCountType()).isEqualTo(WaitCountType.INSTANTANEOUS);
        assertThat(ds.getWaitCount()).isZero(); // darrer tick (i=299 -> 0)
        // El pic puntual wait=6 al segon 42 es preserva amb MAX
        assertThat(ds.getHistory()).anyMatch(s -> Long.valueOf(6L).equals(s.getWaitCount()));
    }

    @Test
    @DisplayName("Finestra 1h amb 3.600 mostres: agrega preservant pics i en fer zoom puja la resolució a 1 s amb preview")
    void getMetrics_finestraUnaHoraIZoom_adaptaResolucioIretornaPreview() {
        AtomicInteger idx = new AtomicInteger(0);
        AtomicInteger cumulativeWait = new AtomicInteger(0);
        when(helper.collectAll()).thenAnswer(inv -> {
            int i = idx.getAndIncrement();
            int inUse = 2;
            if (i == 1_205 || i == 1_206) {
                inUse = 48;
                cumulativeWait.addAndGet(i == 1_205 ? 10 : 5);
            }
            return List.of(jbossSample(
                    "comandaDB", true, 5, 50, inUse, 15,
                    (long) cumulativeWait.get(), i >= 1_205 ? 95L : 10L
            ));
        });

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 19, 10, 0, 0);
        for (int i = 0; i < 3_600; i++) {
            service.collectMetricsAt(t0.plusSeconds(i));
        }

        // 1) Consulta 1h completa -> bucket 20s, <= 181 punts, sense preview, pic preservat (inUse=48, wait sum=15)
        PerformanceMetricsDto fullHour = service.getMetrics(3_600, null, null, 180);
        assertThat(fullHour.getWindowSeconds()).isEqualTo(3_600);
        assertThat(fullHour.getBucketSeconds()).isEqualTo(20);
        DatasourcePoolMetricsDto fullDs = fullHour.getDatasources().get(0);
        assertThat(fullDs.getPreview()).isNull();
        assertThat(fullDs.getHistory().size()).isBetween(179, 181);

        LocalDateTime expectedSpikeBucketTs = t0.plusSeconds(1_200);
        DatasourcePoolSampleDto spikeBucket = fullDs.getHistory().stream()
                .filter(s -> expectedSpikeBucketTs.equals(s.getTimestamp()))
                .findFirst()
                .orElseThrow();
        assertThat(spikeBucket.getInUseCount()).isEqualTo(48);
        assertThat(spikeBucket.getWaitCount()).isEqualTo(15L);

        // 2) Zoom dins [10:19:00 .. 10:21:00] (121 segons) -> resolució cau a 1 s i retorna preview amb la mateixa resolució que la vista completa!
        LocalDateTime zoomFrom = t0.plusSeconds(1_140);
        LocalDateTime zoomTo = t0.plusSeconds(1_260);
        PerformanceMetricsDto zoomed = service.getMetrics(3_600, zoomFrom, zoomTo, 180);
        assertThat(zoomed.getBucketSeconds()).isEqualTo(1);
        assertThat(zoomed.getPreviewBucketSeconds()).isEqualTo(20);
        assertThat(zoomed.getRangeStart()).isEqualTo(zoomFrom);
        assertThat(zoomed.getRangeEnd()).isEqualTo(zoomTo);

        DatasourcePoolMetricsDto zoomedDs = zoomed.getDatasources().get(0);
        assertThat(zoomedDs.getHistory()).hasSize(121);
        assertThat(zoomedDs.getHistory().get(65).getInUseCount()).isEqualTo(48);
        assertThat(zoomedDs.getHistory().get(65).getWaitCount()).isEqualTo(10L);
        assertThat(zoomedDs.getHistory().get(66).getWaitCount()).isEqualTo(5L);
        assertThat(zoomedDs.getPreview()).isNotNull();
        assertThat(zoomedDs.getPreview().size()).isBetween(179, 181);
    }

    @Test
    @DisplayName("Zoom congelat en finestra curta: respecta from/to dins retenció encara que lastCollectedAt avanci")
    void getMetrics_finestraCurtaAmbZoomCongelat_noRetallaFromDinsRetencio() {
        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", true, 5, 50, 3, 8, 0L, 10L)
        ));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 24, 10, 0, 0);
        for (int i = 0; i < 300; i++) {
            service.collectMetricsAt(t0.plusSeconds(i));
        }

        // L'usuari tenia finestra 2m (120s) fins a t0+239 i fa zoom dins [t0+130 .. t0+170],
        // mentre el servidor ja ha avançat fins a t0+299 (windowStart = t0+179).
        LocalDateTime frozenFrom = t0.plusSeconds(130);
        LocalDateTime frozenTo = t0.plusSeconds(170);
        PerformanceMetricsDto zoomed = service.getMetrics(120, frozenFrom, frozenTo, 180);

        assertThat(zoomed.getRangeStart()).isEqualTo(frozenFrom);
        assertThat(zoomed.getRangeEnd()).isEqualTo(frozenTo);
        List<DatasourcePoolSampleDto> history = zoomed.getDatasources().get(0).getHistory();
        assertThat(history).hasSize(41);
        assertThat(history.get(0).getTimestamp()).isEqualTo(frozenFrom);
        assertThat(history.get(history.size() - 1).getTimestamp()).isEqualTo(frozenTo);
    }

    @Test
    @DisplayName("Multi-DS amb estadístiques desactivades: retorna tots els pools però sense històric als desactivats")
    void collectMetrics_multiDsMixt_mantéDisabledSenseHistoric() {
        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", true, 5, 50, 3, 8, 5L, 20L),
                jbossSample("ExampleDS", false, 1, 20, null, null, null, null)
        ));

        PerformanceMetricsDto metrics = service.getMetrics(); // lazy init

        assertThat(metrics.getDatasources()).hasSize(2);
        DatasourcePoolMetricsDto enabled = metrics.getDatasources().get(0);
        DatasourcePoolMetricsDto disabled = metrics.getDatasources().get(1);

        assertThat(enabled.isStatisticsEnabled()).isTrue();
        assertThat(enabled.getHistory()).hasSize(1);

        assertThat(disabled.getPoolName()).isEqualTo("ExampleDS");
        assertThat(disabled.isStatisticsEnabled()).isFalse();
        assertThat(disabled.getMinSize()).isEqualTo(1);
        assertThat(disabled.getMaxSize()).isEqualTo(20);
        assertThat(disabled.getHistory()).isEmpty();
        assertThat(disabled.getPreview()).isNull();
        assertThat(disabled.getWaitCount()).isNull();
    }

    @Test
    @DisplayName("Limita el buffer d'històric a MAX_HISTORY_POINTS (3.600)")
    void collectMetrics_limitaBufferHistoric() {
        when(helper.collectAll()).thenAnswer(inv ->
                List.of(hikariSample("MainHikari", 1L, 5L))
        );

        LocalDateTime start = LocalDateTime.of(2026, 11, 19, 10, 0, 0);
        int total = PerformanceMetricsServiceImpl.MAX_HISTORY_POINTS + 20;
        for (int i = 0; i < total; i++) {
            service.collectMetricsAt(start.plusSeconds(i));
        }

        // maxPoints=600, window=3600 -> bucket 10s (360 punts), però comprovem que la primera mostra guardada és start+20s
        PerformanceMetricsDto zoomedStart = service.getMetrics(3_600, start, start.plusSeconds(100), 180);
        assertThat(zoomedStart.getDatasources().get(0).getHistory().get(0).getTimestamp())
                .isEqualTo(start.plusSeconds(20));
    }

    @Test
    @DisplayName("scheduledCollect: no s'executa si schedulerLeader=false")
    void scheduledCollect_quanNoEsLeader_ometRecolleccio() {
        ReflectionTestUtils.setField(service, "schedulerLeader", false);

        service.scheduledCollect();

        verify(helper, never()).collectAll();
    }

    @Test
    @DisplayName("Workers: calcula delta de completedTasks, gestiona reinicis i agrega per MAX/MIN/SUM amb preview")
    void collectMetrics_workerPools_calculaDeltasIAgregaCorrectament() {
        AtomicLong completed = new AtomicLong(100L);
        when(workerHelper.collectAll()).thenAnswer(inv -> List.of(
                workerSample("salutWorkerExecutor", WorkerPoolType.EXECUTOR, 5, 10, 4, 3, 2, 1000, completed.getAndAdd(4L))
        ));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 20, 9, 0, 0);
        for (int i = 0; i < 3_600; i++) {
            service.collectMetricsAt(t0.plusSeconds(i));
        }

        // Reinici puntual del comptador (simulació de nou valor menor que l'anterior)
        when(workerHelper.collectAll()).thenReturn(List.of(
                workerSample("salutWorkerExecutor", WorkerPoolType.EXECUTOR, 5, 10, 10, 10, 25, 1000, 7L)
        ));
        service.collectMetricsAt(t0.plusSeconds(3_600));

        // 1) Finestra d'1h -> bucket 20s, últim bucket reflecteix el pic active=10, queue=25 i reinici=7
        PerformanceMetricsDto fullHour = service.getMetrics(3_600, null, null, 180);
        assertThat(fullHour.getBucketSeconds()).isEqualTo(20);
        assertThat(fullHour.getWorkerPools()).hasSize(1);
        WorkerPoolMetricsDto wp = fullHour.getWorkerPools().get(0);
        assertThat(wp.getPoolName()).isEqualTo("salutWorkerExecutor");
        assertThat(wp.getPoolType()).isEqualTo(WorkerPoolType.EXECUTOR);
        assertThat(wp.getCompletedTasks()).isEqualTo(7L);
        assertThat(wp.getQueueSize()).isEqualTo(25);
        assertThat(wp.getQueueCapacity()).isEqualTo(1000);
        assertThat(wp.getPreview()).isNull();

        // Al segon tick (dins el primer bucket complet de 20s) s'acumulen 4 tasques/segon
        assertThat(wp.getHistory()).anySatisfy(sample ->
                assertThat(sample.getCompletedTasks()).isEqualTo(80L));

        // 2) Zoom retorna resolució 1s i omple preview
        LocalDateTime zoomFrom = t0.plusSeconds(3_500);
        LocalDateTime zoomTo = t0.plusSeconds(3_600);
        PerformanceMetricsDto zoomed = service.getMetrics(3_600, zoomFrom, zoomTo, 180);
        assertThat(zoomed.getBucketSeconds()).isEqualTo(1);
        WorkerPoolMetricsDto zoomedWp = zoomed.getWorkerPools().get(0);
        assertThat(zoomedWp.getHistory()).hasSize(101);
        assertThat(zoomedWp.getHistory().get(100).getCompletedTasks()).isEqualTo(7L);
        assertThat(zoomedWp.getPreview()).isNotNull();
    }

    @Test
    @DisplayName("JMS Queues i datasources desactivats: bucketSeconds i preview s'adapten igualment i calcula deltes")
    void getMetrics_quanDatasourcesDesactivatsIWorkersJmsActius_calculaDeltasIBuckets() {
        AtomicLong added = new AtomicLong(50L);
        AtomicLong acked = new AtomicLong(40L);
        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", false, 5, 50, null, null, null, null)
        ));
        when(workerHelper.collectAll()).thenAnswer(inv -> List.of(
                workerSample("monitorTaskScheduler", WorkerPoolType.SCHEDULER, 10, 10, 2, 1, 0, null, 10L)
        ));
        when(jmsHelper.collectAll()).thenAnswer(inv -> List.of(
                jmsSample("tasquesQueue", 1, 5, 50, 6, 3, 7L, 10L, added.getAndAdd(3L), acked.getAndAdd(2L))
        ));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 20, 9, 0, 0);
        for (int i = 0; i < 600; i++) {
            service.collectMetricsAt(t0.plusSeconds(i));
        }

        // 600s amb maxPoints=180 -> bucket 5s (tot i que tots els DS tenen statisticsEnabled=false!)
        PerformanceMetricsDto metrics = service.getMetrics(600, null, null, 180);
        assertThat(metrics.getBucketSeconds()).isEqualTo(5);
        assertThat(metrics.getDatasources()).hasSize(1);
        assertThat(metrics.getDatasources().get(0).isStatisticsEnabled()).isFalse();
        assertThat(metrics.getWorkerPools()).hasSize(1);
        assertThat(metrics.getWorkerPools().get(0).getQueueCapacity()).isNull();

        assertThat(metrics.getJmsQueues()).hasSize(1);
        JmsQueueMetricsDto jms = metrics.getJmsQueues().get(0);
        assertThat(jms.getQueueName()).isEqualTo("tasquesQueue");
        assertThat(jms.getListenerCount()).isEqualTo(1);
        assertThat(jms.getAddedMessages()).isEqualTo(3L);
        assertThat(jms.getCompletedMessages()).isEqualTo(2L);
        // En buckets complets de 5s: sum(added)=15, sum(completed)=10
        assertThat(jms.getHistory()).anySatisfy(s -> {
            assertThat(s.getAddedMessages()).isEqualTo(15L);
            assertThat(s.getCompletedMessages()).isEqualTo(10L);
        });

        // Zoom sobre [t0+500 .. t0+599] -> bucketSeconds = 1 i preview omplert
        PerformanceMetricsDto zoomed = service.getMetrics(600, t0.plusSeconds(500), t0.plusSeconds(599), 180);
        assertThat(zoomed.getBucketSeconds()).isEqualTo(1);
        assertThat(zoomed.getJmsQueues().get(0).getPreview()).isNotNull();
        assertThat(zoomed.getWorkerPools().get(0).getPreview()).isNotNull();
    }

    @Test
    @DisplayName("exportHistory retorna tot l'històric en memòria a resolució 1s per a totes les categories")
    void exportHistory_retornaHistoricCompletSenseAgregacioAmbTotesLesCategories() {
        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", true, 5, 50, 4, 12, 10L, 25L),
                jbossSample("ExampleDS", false, 1, 20, null, null, null, null)
        ));
        when(workerHelper.collectAll()).thenReturn(List.of(
                workerSample("salutWorkerExecutor", WorkerPoolType.EXECUTOR, 5, 10, 5, 2, 1, 1000, 20L)
        ));
        when(jmsHelper.collectAll()).thenReturn(List.of(
                jmsSample("tasquesQueue", 1, 5, 50, 5, 1, 2L, 3L, 8L, 6L)
        ));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 20, 9, 0, 0);
        for (int i = 0; i < 400; i++) {
            service.collectMetricsAt(t0.plusSeconds(i));
        }

        PerformanceMetricsDto exported = service.exportHistory();
        LocalDateTime expectedEnd = t0.plusSeconds(399);
        assertThat(exported.getBucketSeconds()).isEqualTo(1);
        assertThat(exported.getWindowSeconds()).isEqualTo(399);
        assertThat(exported.getWindowStart()).isEqualTo(t0);
        assertThat(exported.getWindowEnd()).isEqualTo(expectedEnd);
        assertThat(exported.getRangeStart()).isEqualTo(t0);
        assertThat(exported.getRangeEnd()).isEqualTo(expectedEnd);

        assertThat(exported.getDatasources()).hasSize(2);
        DatasourcePoolMetricsDto enabledDs = exported.getDatasources().get(0);
        DatasourcePoolMetricsDto disabledDs = exported.getDatasources().get(1);
        assertThat(enabledDs.getHistory()).hasSize(400);
        assertThat(enabledDs.getPreview()).isNull();
        assertThat(disabledDs.getHistory()).isEmpty();
        assertThat(disabledDs.getPreview()).isNull();

        assertThat(exported.getWorkerPools()).hasSize(1);
        assertThat(exported.getWorkerPools().get(0).getHistory()).hasSize(400);
        assertThat(exported.getWorkerPools().get(0).getPreview()).isNull();

        assertThat(exported.getJmsQueues()).hasSize(1);
        assertThat(exported.getJmsQueues().get(0).getHistory()).hasSize(400);
        assertThat(exported.getJmsQueues().get(0).getPreview()).isNull();
    }

    @Test
    @DisplayName("exportHistory executa una recol·lecció inicial quan encara no hi ha cap mostra")
    void exportHistory_senseMostresPrevies_executaRecolleccioInicial() {
        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", true, 5, 50, 1, 5, 0L, 5L)
        ));

        PerformanceMetricsDto exported = service.exportHistory();

        verify(helper, times(1)).collectAll();
        assertThat(exported.getCollectedAt()).isNotNull();
        assertThat(exported.getBucketSeconds()).isEqualTo(1);
        assertThat(exported.getWindowSeconds()).isZero();
        assertThat(exported.getDatasources()).hasSize(1);
        assertThat(exported.getDatasources().get(0).getHistory()).hasSize(1);
    }

    @Test
    @DisplayName("Quan el paràmetre es desactiva, s'atura la recol·lecció, s'esborra l'històric i estimatedMemoryBytes passa a 0")
    void scheduledCollectIGetMetrics_quanParametreDesactivat_esborraHistoricIRetornaZeroBytes() {
        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", true, 5, 50, 4, 12, 10L, 25L)
        ));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 24, 10, 0, 0);
        for (int i = 0; i < 5; i++) {
            service.collectMetricsAt(t0.plusSeconds(i));
        }

        PerformanceMetricsDto activeMetrics = service.getMetrics();
        assertThat(activeMetrics.isCollectionEnabled()).isTrue();
        assertThat(activeMetrics.getEstimatedMemoryBytes()).isGreaterThan(0L);
        assertThat(activeMetrics.getDatasources()).hasSize(1);
        assertThat(activeMetrics.getDatasources().get(0).getHistory()).hasSize(5);

        // Desactivem pel paràmetre de BD
        when(parametresHelper.getParametreBoolean(BaseConfig.PROP_MONITOR_RENDIMENT_ACTIU, true))
                .thenReturn(false);

        service.scheduledCollect();
        verify(helper, times(5)).collectAll();

        PerformanceMetricsDto disabledMetrics = service.getMetrics();
        assertThat(disabledMetrics.isCollectionEnabled()).isFalse();
        assertThat(disabledMetrics.getEstimatedMemoryBytes()).isZero();
        assertThat(disabledMetrics.getCollectedAt()).isNull();
        assertThat(disabledMetrics.getDatasources()).isEmpty();
        assertThat(disabledMetrics.getWorkerPools()).isEmpty();
        assertThat(disabledMetrics.getJmsQueues()).isEmpty();
        assertThat(service.estimateRetainedMemoryBytesUpperBound()).isZero();

        PerformanceMetricsDto disabledExport = service.exportHistory();
        assertThat(disabledExport.isCollectionEnabled()).isFalse();
        assertThat(disabledExport.getEstimatedMemoryBytes()).isZero();
        assertThat(disabledExport.getDatasources()).isEmpty();

        // En reactivar-lo, torna a recollir des de zero
        when(parametresHelper.getParametreBoolean(BaseConfig.PROP_MONITOR_RENDIMENT_ACTIU, true))
                .thenReturn(true);
        service.collectMetricsAt(t0.plusSeconds(10));
        verify(helper, times(6)).collectAll();

        PerformanceMetricsDto resumed = service.getMetrics();
        assertThat(resumed.isCollectionEnabled()).isTrue();
        assertThat(resumed.getEstimatedMemoryBytes()).isGreaterThan(0L);
        assertThat(resumed.getDatasources().get(0).getHistory()).hasSize(1);
    }

    @Test
    @DisplayName("estimateRetainedMemoryBytesUpperBound creix monòtonament amb les mostres i torna a zero amb clearHistory")
    void estimateRetainedMemoryBytesUpperBound_creixAmbLesMostresIRetornaZeroQuanBuit() {
        assertThat(service.estimateRetainedMemoryBytesUpperBound()).isZero();

        when(helper.collectAll()).thenReturn(List.of(
                jbossSample("comandaDB", true, 5, 50, 2, 10, 0L, 10L)
        ));
        when(workerHelper.collectAll()).thenReturn(List.of(
                workerSample("salutWorkerExecutor", WorkerPoolType.EXECUTOR, 5, 10, 5, 1, 0, 1000, 1L)
        ));
        when(jmsHelper.collectAll()).thenReturn(List.of(
                jmsSample("tasquesQueue", 1, 5, 50, 5, 0, 0L, 0L, 0L, 0L)
        ));

        LocalDateTime t0 = LocalDateTime.of(2026, 11, 24, 10, 0, 0);
        service.collectMetricsAt(t0);
        long bytesAfter1 = service.estimateRetainedMemoryBytesUpperBound();
        assertThat(bytesAfter1).isGreaterThan(0L);

        service.collectMetricsAt(t0.plusSeconds(1));
        long bytesAfter2 = service.estimateRetainedMemoryBytesUpperBound();
        assertThat(bytesAfter2).isGreaterThan(bytesAfter1);

        service.clearHistory();
        assertThat(service.estimateRetainedMemoryBytesUpperBound()).isZero();
    }

    private RawDatasourcePoolSample jbossSample(
            String name,
            boolean statsEnabled,
            Integer min,
            Integer max,
            Integer inUse,
            Integer active,
            Long rawWait,
            Long maxWaitMs) {
        RawDatasourcePoolSample sample = new RawDatasourcePoolSample();
        sample.setPoolKey("jboss:" + name);
        sample.setPoolName(name);
        sample.setJndiName("java:jboss/datasources/" + name);
        sample.setStatisticsEnabled(statsEnabled);
        sample.setWaitCountType(WaitCountType.INTERVAL_DELTA);
        sample.setMinSize(min);
        sample.setMaxSize(max);
        sample.setInUseCount(inUse);
        sample.setActiveCount(active);
        sample.setRawWaitCount(rawWait);
        sample.setMaxWaitTimeMs(maxWaitMs);
        return sample;
    }

    private RawDatasourcePoolSample hikariSample(String name, Long waitCount, Long maxWaitTimeMs) {
        RawDatasourcePoolSample sample = new RawDatasourcePoolSample();
        sample.setPoolKey("hikari:" + name);
        sample.setPoolName(name);
        sample.setStatisticsEnabled(true);
        sample.setWaitCountType(WaitCountType.INSTANTANEOUS);
        sample.setMinSize(2);
        sample.setMaxSize(10);
        sample.setInUseCount(1);
        sample.setActiveCount(2);
        sample.setRawWaitCount(waitCount);
        sample.setMaxWaitTimeMs(maxWaitTimeMs);
        return sample;
    }

    private RawWorkerPoolSample workerSample(
            String name,
            WorkerPoolType type,
            Integer core,
            Integer max,
            Integer poolSize,
            Integer active,
            Integer queueSize,
            Integer queueCapacity,
            Long completed) {
        RawWorkerPoolSample sample = new RawWorkerPoolSample();
        sample.setPoolKey("worker:" + name);
        sample.setPoolName(name);
        sample.setThreadNamePrefix(name + "-");
        sample.setPoolType(type);
        sample.setCoreSize(core);
        sample.setMaxSize(max);
        sample.setPoolSize(poolSize);
        sample.setActiveCount(active);
        sample.setQueueSize(queueSize);
        sample.setQueueCapacity(queueCapacity);
        sample.setRawCompletedTaskCount(completed);
        return sample;
    }

    private RawJmsQueueSample jmsSample(
            String queueName,
            int listeners,
            Integer minConsumers,
            Integer maxConsumers,
            Integer consumers,
            Integer delivering,
            Long pending,
            Long total,
            Long added,
            Long acked) {
        RawJmsQueueSample sample = new RawJmsQueueSample();
        sample.setPoolKey("jms:" + queueName);
        sample.setQueueName(queueName);
        sample.setListenerCount(listeners);
        sample.setMinConsumers(minConsumers);
        sample.setMaxConsumers(maxConsumers);
        sample.setConsumerCount(consumers);
        sample.setDeliveringCount(delivering);
        sample.setPendingCount(pending);
        sample.setMessageCount(total);
        sample.setRawMessagesAdded(added);
        sample.setRawMessagesAcknowledged(acked);
        return sample;
    }
}
