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
import es.caib.comanda.monitor.logic.intf.model.performance.JmsQueueSampleDto;
import es.caib.comanda.monitor.logic.intf.model.performance.PerformanceMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WaitCountType;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolSampleDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolType;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Servei de mostreig en memòria de les mètriques internes de rendiment
 * (pools de BD, pools de workers i cues JMS).
 * <p>
 * Mostra cada 1 s a la instància líder ({@code es.caib.comanda.scheduler.leader}) amb una
 * finestra circular en memòria d'1 hora (3.600 punts) i agregació adaptativa per buckets sota
 * demanda quan es consulta un rang temporal ampli o amb zoom.
 */
@Service
@Slf4j
public class PerformanceMetricsServiceImpl {

    static final int SAMPLING_INTERVAL_SECONDS = 1;
    static final int MAX_RETENTION_SECONDS = 3_600;
    static final int MAX_HISTORY_POINTS = 3_600;
    static final int DEFAULT_WINDOW_SECONDS = 300;
    static final int MIN_WINDOW_SECONDS = 60;
    static final int DEFAULT_MAX_POINTS = 180;
    static final int MIN_MAX_POINTS = 30;
    static final int MAX_ALLOWED_POINTS = 600;
    static final int PREVIEW_MAX_POINTS = DEFAULT_MAX_POINTS;

    private static final int[] NICE_BUCKET_SECONDS = { 1, 2, 5, 10, 15, 20, 30, 60 };

    @Autowired
    private DatasourcePoolMetricsHelper datasourcePoolMetricsHelper;

    @Autowired(required = false)
    private WorkerPoolMetricsHelper workerPoolMetricsHelper;

    @Autowired(required = false)
    private JmsQueueMetricsHelper jmsQueueMetricsHelper;

    @Autowired(required = false)
    private ParametresHelper parametresHelper;

    @Value("${" + BaseConfig.PROP_SCHEDULER_LEADER + ":#{true}}")
    private boolean schedulerLeader = true;

    private final JvmMemoryLayout jvmMemoryLayout = JvmMemoryLayout.detect();
    private final Map<String, DatasourceState> datasourceStates = new LinkedHashMap<>();
    private final Map<String, WorkerPoolState> workerPoolStates = new LinkedHashMap<>();
    private final Map<String, JmsQueueState> jmsQueueStates = new LinkedHashMap<>();
    private LocalDateTime lastCollectedAt;

    @Scheduled(fixedDelay = 1_000, initialDelay = 2_000)
    public void scheduledCollect() {
        if (!schedulerLeader) {
            return;
        }
        if (!isCollectionEnabled()) {
            clearHistory();
            return;
        }
        collectMetrics();
    }

    /**
     * Indica si la recol·lecció de mètriques en memòria està activada pel paràmetre de configuració.
     */
    public boolean isCollectionEnabled() {
        if (parametresHelper == null) {
            return true;
        }
        try {
            Boolean enabled = parametresHelper.getParametreBoolean(BaseConfig.PROP_MONITOR_RENDIMENT_ACTIU, true);
            return !Boolean.FALSE.equals(enabled);
        } catch (Exception e) {
            log.warn("No s'ha pogut llegir el paràmetre '{}'. Es manté activada la recol·lecció: {}",
                    BaseConfig.PROP_MONITOR_RENDIMENT_ACTIU, e.getMessage());
            return true;
        }
    }

    /**
     * Esborra tot l'històric acumulat en memòria de datasources, worker pools i cues JMS.
     */
    public synchronized void clearHistory() {
        datasourceStates.clear();
        workerPoolStates.clear();
        jmsQueueStates.clear();
        lastCollectedAt = null;
    }

    /**
     * Calcula una cota superior conservadora (en bytes) de la memòria RAM retinguda per
     * l'històric en memòria segons el layout JVM detectat.
     */
    synchronized long estimateRetainedMemoryBytesUpperBound() {
        if (datasourceStates.isEmpty() && workerPoolStates.isEmpty() && jmsQueueStates.isEmpty()) {
            return 0L;
        }
        long total = 0L;
        int maxHistorySize = 0;

        for (DatasourceState state : datasourceStates.values()) {
            int size = state.history.size();
            maxHistorySize = Math.max(maxHistorySize, size);
            total += jvmMemoryLayout.stateEntryOverheadBytes
                    + jvmMemoryLayout.arrayDequeBackingBytes(size)
                    + (long) size * jvmMemoryLayout.datasourceSampleUpperBoundBytes;
        }
        for (WorkerPoolState state : workerPoolStates.values()) {
            int size = state.history.size();
            maxHistorySize = Math.max(maxHistorySize, size);
            total += jvmMemoryLayout.stateEntryOverheadBytes
                    + jvmMemoryLayout.arrayDequeBackingBytes(size)
                    + (long) size * jvmMemoryLayout.workerSampleUpperBoundBytes;
        }
        for (JmsQueueState state : jmsQueueStates.values()) {
            int size = state.history.size();
            maxHistorySize = Math.max(maxHistorySize, size);
            total += jvmMemoryLayout.stateEntryOverheadBytes
                    + jvmMemoryLayout.arrayDequeBackingBytes(size)
                    + (long) size * jvmMemoryLayout.jmsSampleUpperBoundBytes;
        }

        total += (long) maxHistorySize * jvmMemoryLayout.localDateTimeTreeBytes;
        return total;
    }

    /**
     * Executa un cicle síncron de mostreig utilitzant l'hora actual.
     */
    public synchronized void collectMetrics() {
        collectMetricsAt(LocalDateTime.now());
    }

    /**
     * Executa un cicle síncron de mostreig amb un timestamp explícit (útil per a proves deterministes).
     */
    synchronized void collectMetricsAt(LocalDateTime sampleTime) {
        if (!isCollectionEnabled()) {
            clearHistory();
            return;
        }
        collectDatasources(sampleTime);
        collectWorkerPools(sampleTime);
        collectJmsQueues(sampleTime);
        lastCollectedAt = sampleTime;
    }

    private void collectDatasources(LocalDateTime sampleTime) {
        List<RawDatasourcePoolSample> rawSamples = datasourcePoolMetricsHelper != null
                ? datasourcePoolMetricsHelper.collectAll()
                : Collections.emptyList();

        Set<String> activeKeys = new HashSet<>(rawSamples.size());
        for (RawDatasourcePoolSample raw : rawSamples) {
            activeKeys.add(raw.getPoolKey());
            DatasourceState state = datasourceStates.computeIfAbsent(raw.getPoolKey(), k -> new DatasourceState());
            updateDatasourceState(raw, state, sampleTime);
        }
        datasourceStates.keySet().retainAll(activeKeys);
    }

    private void collectWorkerPools(LocalDateTime sampleTime) {
        List<RawWorkerPoolSample> rawSamples = workerPoolMetricsHelper != null
                ? workerPoolMetricsHelper.collectAll()
                : Collections.emptyList();

        Set<String> activeKeys = new HashSet<>(rawSamples.size());
        for (RawWorkerPoolSample raw : rawSamples) {
            activeKeys.add(raw.getPoolKey());
            WorkerPoolState state = workerPoolStates.computeIfAbsent(raw.getPoolKey(), k -> new WorkerPoolState());
            updateWorkerPoolState(raw, state, sampleTime);
        }
        workerPoolStates.keySet().retainAll(activeKeys);
    }

    private void collectJmsQueues(LocalDateTime sampleTime) {
        List<RawJmsQueueSample> rawSamples = jmsQueueMetricsHelper != null
                ? jmsQueueMetricsHelper.collectAll()
                : Collections.emptyList();

        Set<String> activeKeys = new HashSet<>(rawSamples.size());
        for (RawJmsQueueSample raw : rawSamples) {
            activeKeys.add(raw.getPoolKey());
            JmsQueueState state = jmsQueueStates.computeIfAbsent(raw.getPoolKey(), k -> new JmsQueueState());
            updateJmsQueueState(raw, state, sampleTime);
        }
        jmsQueueStates.keySet().retainAll(activeKeys);
    }

    /**
     * Retorna la vista per defecte (finestra de 5 min, màxim 180 punts agregats).
     */
    public PerformanceMetricsDto getMetrics() {
        return getMetrics(DEFAULT_WINDOW_SECONDS, null, null, DEFAULT_MAX_POINTS);
    }

    /**
     * Retorna les mètriques dins la finestra temporal sol·licitada, amb zoom opcional ({@code from}/{@code to})
     * i agregació adaptativa per buckets per preservar pics sense sobrecarregar la xarxa ni el navegador.
     */
    public synchronized PerformanceMetricsDto getMetrics(
            Integer windowSeconds,
            LocalDateTime from,
            LocalDateTime to,
            Integer maxPoints) {

        int effWindow = clamp(windowSeconds != null ? windowSeconds : DEFAULT_WINDOW_SECONDS,
                MIN_WINDOW_SECONDS, MAX_RETENTION_SECONDS);
        int effMaxPoints = clamp(maxPoints != null ? maxPoints : DEFAULT_MAX_POINTS,
                MIN_MAX_POINTS, MAX_ALLOWED_POINTS);

        if (!isCollectionEnabled()) {
            clearHistory();
            return new PerformanceMetricsDto(
                    null,
                    SAMPLING_INTERVAL_SECONDS,
                    MAX_HISTORY_POINTS,
                    effWindow,
                    1,
                    1,
                    false,
                    0L,
                    null,
                    null,
                    null,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        if (lastCollectedAt == null) {
            collectMetrics();
        }

        LocalDateTime windowEnd = lastCollectedAt;
        LocalDateTime windowStart = windowEnd.minusSeconds(effWindow);
        LocalDateTime retentionStart = windowEnd.minusSeconds(MAX_RETENTION_SECONDS);

        boolean zoomed = false;
        LocalDateTime rangeStart = windowStart;
        LocalDateTime rangeEnd = windowEnd;

        if (from != null && to != null && !to.isBefore(from)) {
            LocalDateTime candStart = from.isBefore(retentionStart) ? retentionStart : from;
            LocalDateTime candEnd = to.isAfter(windowEnd) ? windowEnd : to;
            if (!candEnd.isBefore(candStart)
                    && (!candStart.equals(windowStart) || !candEnd.equals(windowEnd))) {
                rangeStart = candStart;
                rangeEnd = candEnd;
                zoomed = true;
            }
        }

        RangeStats rangeStats = new RangeStats();
        RangeStats windowStats = new RangeStats();

        List<DatasourceState> dsStates = new ArrayList<>(datasourceStates.values());
        List<List<DatasourcePoolSampleDto>> dsRangeLists = new ArrayList<>(dsStates.size());
        List<List<DatasourcePoolSampleDto>> dsWindowLists = zoomed ? new ArrayList<>(dsStates.size()) : Collections.emptyList();

        for (DatasourceState state : dsStates) {
            if (!state.statisticsEnabled) {
                dsRangeLists.add(Collections.emptyList());
                if (zoomed) {
                    dsWindowLists.add(Collections.emptyList());
                }
                continue;
            }
            List<DatasourcePoolSampleDto> filteredRange = filterRange(state.history, DatasourcePoolSampleDto::getTimestamp, rangeStart, rangeEnd);
            dsRangeLists.add(filteredRange);
            rangeStats.observe(filteredRange, DatasourcePoolSampleDto::getTimestamp);
            if (zoomed) {
                List<DatasourcePoolSampleDto> filteredWindow = filterRange(state.history, DatasourcePoolSampleDto::getTimestamp, windowStart, windowEnd);
                dsWindowLists.add(filteredWindow);
                windowStats.observe(filteredWindow, DatasourcePoolSampleDto::getTimestamp);
            }
        }

        List<WorkerPoolState> wpStates = new ArrayList<>(workerPoolStates.values());
        List<List<WorkerPoolSampleDto>> wpRangeLists = new ArrayList<>(wpStates.size());
        List<List<WorkerPoolSampleDto>> wpWindowLists = zoomed ? new ArrayList<>(wpStates.size()) : Collections.emptyList();

        for (WorkerPoolState state : wpStates) {
            List<WorkerPoolSampleDto> filteredRange = filterRange(state.history, WorkerPoolSampleDto::getTimestamp, rangeStart, rangeEnd);
            wpRangeLists.add(filteredRange);
            rangeStats.observe(filteredRange, WorkerPoolSampleDto::getTimestamp);
            if (zoomed) {
                List<WorkerPoolSampleDto> filteredWindow = filterRange(state.history, WorkerPoolSampleDto::getTimestamp, windowStart, windowEnd);
                wpWindowLists.add(filteredWindow);
                windowStats.observe(filteredWindow, WorkerPoolSampleDto::getTimestamp);
            }
        }

        List<JmsQueueState> jmsStates = new ArrayList<>(jmsQueueStates.values());
        List<List<JmsQueueSampleDto>> jmsRangeLists = new ArrayList<>(jmsStates.size());
        List<List<JmsQueueSampleDto>> jmsWindowLists = zoomed ? new ArrayList<>(jmsStates.size()) : Collections.emptyList();

        for (JmsQueueState state : jmsStates) {
            List<JmsQueueSampleDto> filteredRange = filterRange(state.history, JmsQueueSampleDto::getTimestamp, rangeStart, rangeEnd);
            jmsRangeLists.add(filteredRange);
            rangeStats.observe(filteredRange, JmsQueueSampleDto::getTimestamp);
            if (zoomed) {
                List<JmsQueueSampleDto> filteredWindow = filterRange(state.history, JmsQueueSampleDto::getTimestamp, windowStart, windowEnd);
                jmsWindowLists.add(filteredWindow);
                windowStats.observe(filteredWindow, JmsQueueSampleDto::getTimestamp);
            }
        }

        int bucketSeconds = computeBucketSeconds(
                rangeStats.maxSamples,
                spanSeconds(rangeStats.minTs, rangeStats.maxTs),
                effMaxPoints
        );
        int previewBucketSeconds = zoomed
                ? computeBucketSeconds(
                        windowStats.maxSamples,
                        spanSeconds(windowStats.minTs, windowStats.maxTs),
                        effMaxPoints)
                : bucketSeconds;

        List<DatasourcePoolMetricsDto> dsDtos = buildDatasourceDtos(dsStates, dsRangeLists, dsWindowLists, bucketSeconds, previewBucketSeconds, zoomed);
        List<WorkerPoolMetricsDto> wpDtos = buildWorkerPoolDtos(wpStates, wpRangeLists, wpWindowLists, bucketSeconds, previewBucketSeconds, zoomed);
        List<JmsQueueMetricsDto> jmsDtos = buildJmsQueueDtos(jmsStates, jmsRangeLists, jmsWindowLists, bucketSeconds, previewBucketSeconds, zoomed);

        return new PerformanceMetricsDto(
                lastCollectedAt,
                SAMPLING_INTERVAL_SECONDS,
                MAX_HISTORY_POINTS,
                effWindow,
                bucketSeconds,
                previewBucketSeconds,
                true,
                estimateRetainedMemoryBytesUpperBound(),
                windowStart,
                windowEnd,
                rangeStart,
                rangeEnd,
                dsDtos,
                wpDtos,
                jmsDtos
        );
    }

    /**
     * Exporta tot l'històric complet en memòria sense agregació ({@code bucketSeconds = 1})
     * per a totes les categories actives (datasources, worker pools i cues JMS).
     */
    public synchronized PerformanceMetricsDto exportHistory() {
        if (!isCollectionEnabled()) {
            clearHistory();
            return new PerformanceMetricsDto(
                    null,
                    SAMPLING_INTERVAL_SECONDS,
                    MAX_HISTORY_POINTS,
                    0,
                    1,
                    1,
                    false,
                    0L,
                    null,
                    null,
                    null,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        if (lastCollectedAt == null) {
            collectMetrics();
        }

        RangeStats rangeStats = new RangeStats();

        List<DatasourceState> dsStates = new ArrayList<>(datasourceStates.values());
        List<List<DatasourcePoolSampleDto>> dsFullLists = new ArrayList<>(dsStates.size());
        for (DatasourceState state : dsStates) {
            if (!state.statisticsEnabled) {
                dsFullLists.add(Collections.emptyList());
                continue;
            }
            List<DatasourcePoolSampleDto> copy = new ArrayList<>(state.history);
            dsFullLists.add(copy);
            rangeStats.observe(copy, DatasourcePoolSampleDto::getTimestamp);
        }

        List<WorkerPoolState> wpStates = new ArrayList<>(workerPoolStates.values());
        List<List<WorkerPoolSampleDto>> wpFullLists = new ArrayList<>(wpStates.size());
        for (WorkerPoolState state : wpStates) {
            List<WorkerPoolSampleDto> copy = new ArrayList<>(state.history);
            wpFullLists.add(copy);
            rangeStats.observe(copy, WorkerPoolSampleDto::getTimestamp);
        }

        List<JmsQueueState> jmsStates = new ArrayList<>(jmsQueueStates.values());
        List<List<JmsQueueSampleDto>> jmsFullLists = new ArrayList<>(jmsStates.size());
        for (JmsQueueState state : jmsStates) {
            List<JmsQueueSampleDto> copy = new ArrayList<>(state.history);
            jmsFullLists.add(copy);
            rangeStats.observe(copy, JmsQueueSampleDto::getTimestamp);
        }

        LocalDateTime windowEnd = lastCollectedAt;
        LocalDateTime windowStart = rangeStats.minTs != null ? rangeStats.minTs : windowEnd;
        int effWindow = (int) Math.max(0L, spanSeconds(windowStart, windowEnd));

        List<DatasourcePoolMetricsDto> dsDtos = buildDatasourceDtos(dsStates, dsFullLists, Collections.emptyList(), 1, 1, false);
        List<WorkerPoolMetricsDto> wpDtos = buildWorkerPoolDtos(wpStates, wpFullLists, Collections.emptyList(), 1, 1, false);
        List<JmsQueueMetricsDto> jmsDtos = buildJmsQueueDtos(jmsStates, jmsFullLists, Collections.emptyList(), 1, 1, false);

        return new PerformanceMetricsDto(
                lastCollectedAt,
                SAMPLING_INTERVAL_SECONDS,
                MAX_HISTORY_POINTS,
                effWindow,
                1,
                1,
                true,
                estimateRetainedMemoryBytesUpperBound(),
                windowStart,
                windowEnd,
                windowStart,
                windowEnd,
                dsDtos,
                wpDtos,
                jmsDtos
        );
    }

    private static List<DatasourcePoolMetricsDto> buildDatasourceDtos(
            List<DatasourceState> states,
            List<List<DatasourcePoolSampleDto>> rangeLists,
            List<List<DatasourcePoolSampleDto>> windowLists,
            int bucketSeconds,
            int previewBucketSeconds,
            boolean zoomed) {

        List<DatasourcePoolMetricsDto> dtos = new ArrayList<>(states.size());
        for (int i = 0; i < states.size(); i++) {
            DatasourceState state = states.get(i);
            DatasourcePoolMetricsDto dto = new DatasourcePoolMetricsDto();
            dto.setPoolName(state.poolName);
            dto.setJndiName(state.jndiName);
            dto.setStatisticsEnabled(state.statisticsEnabled);
            dto.setWaitCountType(state.waitCountType);
            dto.setMinSize(state.minSize);
            dto.setMaxSize(state.maxSize);

            if (!state.statisticsEnabled) {
                dto.setHistory(Collections.emptyList());
                dtos.add(dto);
                continue;
            }

            dto.setInUseCount(state.inUseCount);
            dto.setActiveCount(state.activeCount);
            dto.setWaitCount(state.waitCount);
            dto.setMaxWaitTimeMs(state.maxWaitTimeMs);
            dto.setHistory(downsample(rangeLists.get(i), bucketSeconds, state.waitCountType));
            if (zoomed) {
                dto.setPreview(downsample(windowLists.get(i), previewBucketSeconds, state.waitCountType));
            }
            dtos.add(dto);
        }
        return Collections.unmodifiableList(dtos);
    }

    private static List<WorkerPoolMetricsDto> buildWorkerPoolDtos(
            List<WorkerPoolState> states,
            List<List<WorkerPoolSampleDto>> rangeLists,
            List<List<WorkerPoolSampleDto>> windowLists,
            int bucketSeconds,
            int previewBucketSeconds,
            boolean zoomed) {

        List<WorkerPoolMetricsDto> dtos = new ArrayList<>(states.size());
        for (int i = 0; i < states.size(); i++) {
            WorkerPoolState state = states.get(i);
            WorkerPoolMetricsDto dto = new WorkerPoolMetricsDto();
            dto.setPoolName(state.poolName);
            dto.setThreadNamePrefix(state.threadNamePrefix);
            dto.setPoolType(state.poolType);
            dto.setCoreSize(state.coreSize);
            dto.setMaxSize(state.maxSize);
            dto.setPoolSize(state.poolSize);
            dto.setActiveCount(state.activeCount);
            dto.setQueueSize(state.queueSize);
            dto.setQueueCapacity(state.queueCapacity);
            dto.setCompletedTasks(state.completedTasks);
            dto.setHistory(downsampleWorkers(rangeLists.get(i), bucketSeconds));
            if (zoomed) {
                dto.setPreview(downsampleWorkers(windowLists.get(i), previewBucketSeconds));
            }
            dtos.add(dto);
        }
        return Collections.unmodifiableList(dtos);
    }

    private static List<JmsQueueMetricsDto> buildJmsQueueDtos(
            List<JmsQueueState> states,
            List<List<JmsQueueSampleDto>> rangeLists,
            List<List<JmsQueueSampleDto>> windowLists,
            int bucketSeconds,
            int previewBucketSeconds,
            boolean zoomed) {

        List<JmsQueueMetricsDto> dtos = new ArrayList<>(states.size());
        for (int i = 0; i < states.size(); i++) {
            JmsQueueState state = states.get(i);
            JmsQueueMetricsDto dto = new JmsQueueMetricsDto();
            dto.setQueueName(state.queueName);
            dto.setListenerCount(state.listenerCount);
            dto.setMinConsumers(state.minConsumers);
            dto.setMaxConsumers(state.maxConsumers);
            dto.setConsumerCount(state.consumerCount);
            dto.setDeliveringCount(state.deliveringCount);
            dto.setPendingCount(state.pendingCount);
            dto.setMessageCount(state.messageCount);
            dto.setCompletedMessages(state.completedMessages);
            dto.setAddedMessages(state.addedMessages);
            dto.setHistory(downsampleJmsQueues(rangeLists.get(i), bucketSeconds));
            if (zoomed) {
                dto.setPreview(downsampleJmsQueues(windowLists.get(i), previewBucketSeconds));
            }
            dtos.add(dto);
        }
        return Collections.unmodifiableList(dtos);
    }

    private void updateDatasourceState(
            RawDatasourcePoolSample raw,
            DatasourceState state,
            LocalDateTime sampleTime) {

        state.poolName = raw.getPoolName();
        state.jndiName = raw.getJndiName();
        state.statisticsEnabled = raw.isStatisticsEnabled();
        state.waitCountType = raw.getWaitCountType();
        state.minSize = raw.getMinSize();
        state.maxSize = raw.getMaxSize();

        if (!raw.isStatisticsEnabled()) {
            state.lastRawWaitCount = null;
            state.inUseCount = null;
            state.activeCount = null;
            state.waitCount = null;
            state.maxWaitTimeMs = null;
            state.history.clear();
            return;
        }

        Long effectiveWaitCount;
        if (raw.getWaitCountType() == WaitCountType.INTERVAL_DELTA) {
            effectiveWaitCount = computeMonotonicDelta(raw.getRawWaitCount(), state.lastRawWaitCount);
            state.lastRawWaitCount = raw.getRawWaitCount();
        } else {
            effectiveWaitCount = raw.getRawWaitCount();
        }

        state.inUseCount = raw.getInUseCount();
        state.activeCount = raw.getActiveCount();
        state.waitCount = effectiveWaitCount;
        state.maxWaitTimeMs = raw.getMaxWaitTimeMs();

        appendBounded(state.history, new DatasourcePoolSampleDto(
                sampleTime,
                raw.getInUseCount(),
                raw.getActiveCount(),
                effectiveWaitCount,
                raw.getMaxWaitTimeMs()
        ));
    }

    private void updateWorkerPoolState(
            RawWorkerPoolSample raw,
            WorkerPoolState state,
            LocalDateTime sampleTime) {

        state.poolName = raw.getPoolName();
        state.threadNamePrefix = raw.getThreadNamePrefix();
        state.poolType = raw.getPoolType();
        state.coreSize = raw.getCoreSize();
        state.maxSize = raw.getMaxSize();
        state.poolSize = raw.getPoolSize();
        state.activeCount = raw.getActiveCount();
        state.queueSize = raw.getQueueSize();
        state.queueCapacity = raw.getQueueCapacity();

        Long completedDelta = computeMonotonicDelta(raw.getRawCompletedTaskCount(), state.lastRawCompletedTaskCount);
        state.lastRawCompletedTaskCount = raw.getRawCompletedTaskCount();
        state.completedTasks = completedDelta;

        appendBounded(state.history, new WorkerPoolSampleDto(
                sampleTime,
                raw.getPoolSize(),
                raw.getActiveCount(),
                raw.getQueueSize(),
                completedDelta
        ));
    }

    private void updateJmsQueueState(
            RawJmsQueueSample raw,
            JmsQueueState state,
            LocalDateTime sampleTime) {

        state.queueName = raw.getQueueName();
        state.listenerCount = raw.getListenerCount();
        state.minConsumers = raw.getMinConsumers();
        state.maxConsumers = raw.getMaxConsumers();
        state.consumerCount = raw.getConsumerCount();
        state.deliveringCount = raw.getDeliveringCount();
        state.pendingCount = raw.getPendingCount();
        state.messageCount = raw.getMessageCount();

        Long completedDelta = computeMonotonicDelta(raw.getRawMessagesAcknowledged(), state.lastRawMessagesAcknowledged);
        state.lastRawMessagesAcknowledged = raw.getRawMessagesAcknowledged();
        state.completedMessages = completedDelta;

        Long addedDelta = computeMonotonicDelta(raw.getRawMessagesAdded(), state.lastRawMessagesAdded);
        state.lastRawMessagesAdded = raw.getRawMessagesAdded();
        state.addedMessages = addedDelta;

        appendBounded(state.history, new JmsQueueSampleDto(
                sampleTime,
                raw.getConsumerCount(),
                raw.getDeliveringCount(),
                raw.getPendingCount(),
                completedDelta,
                addedDelta
        ));
    }

    private static <T> void appendBounded(Deque<T> history, T sample) {
        history.addLast(sample);
        while (history.size() > MAX_HISTORY_POINTS) {
            history.pollFirst();
        }
    }

    static Long computeMonotonicDelta(Long rawValue, Long previousRawValue) {
        if (rawValue == null) {
            return null;
        }
        if (previousRawValue == null) {
            return 0L;
        }
        return rawValue >= previousRawValue ? rawValue - previousRawValue : rawValue;
    }

    private static <T> List<T> filterRange(
            Deque<T> history,
            Function<T, LocalDateTime> tsExtractor,
            LocalDateTime start,
            LocalDateTime end) {

        List<T> filtered = new ArrayList<>();
        for (T sample : history) {
            LocalDateTime ts = tsExtractor.apply(sample);
            if (!ts.isBefore(start) && !ts.isAfter(end)) {
                filtered.add(sample);
            }
        }
        return filtered;
    }

    private static long spanSeconds(LocalDateTime minTs, LocalDateTime maxTs) {
        if (minTs == null || maxTs == null || !maxTs.isAfter(minTs)) {
            return 0L;
        }
        return Duration.between(minTs, maxTs).getSeconds();
    }

    private static int computeBucketSeconds(int sampleCount, long spanSeconds, int maxPoints) {
        if (sampleCount <= maxPoints || spanSeconds <= 0L) {
            return 1;
        }
        for (int candidate : NICE_BUCKET_SECONDS) {
            if ((spanSeconds / candidate) + 1L <= maxPoints) {
                return candidate;
            }
        }
        return Math.max(1, (int) Math.ceil((double) spanSeconds / maxPoints));
    }

    static List<DatasourcePoolSampleDto> downsample(
            List<DatasourcePoolSampleDto> samples,
            int bucketSeconds,
            WaitCountType waitCountType) {

        boolean sumWaitCount = waitCountType == WaitCountType.INTERVAL_DELTA;
        return downsampleGeneric(
                samples,
                bucketSeconds,
                DatasourcePoolSampleDto::getTimestamp,
                bucketTs -> new DatasourceBucketAccumulator(bucketTs, sumWaitCount),
                DatasourceBucketAccumulator::accept,
                DatasourceBucketAccumulator::toDto
        );
    }

    static List<WorkerPoolSampleDto> downsampleWorkers(
            List<WorkerPoolSampleDto> samples,
            int bucketSeconds) {

        return downsampleGeneric(
                samples,
                bucketSeconds,
                WorkerPoolSampleDto::getTimestamp,
                WorkerBucketAccumulator::new,
                WorkerBucketAccumulator::accept,
                WorkerBucketAccumulator::toDto
        );
    }

    static List<JmsQueueSampleDto> downsampleJmsQueues(
            List<JmsQueueSampleDto> samples,
            int bucketSeconds) {

        return downsampleGeneric(
                samples,
                bucketSeconds,
                JmsQueueSampleDto::getTimestamp,
                JmsBucketAccumulator::new,
                JmsBucketAccumulator::accept,
                JmsBucketAccumulator::toDto
        );
    }

    private static <S, A> List<S> downsampleGeneric(
            List<S> samples,
            int bucketSeconds,
            Function<S, LocalDateTime> tsExtractor,
            Function<LocalDateTime, A> accumulatorFactory,
            java.util.function.BiConsumer<A, S> consumer,
            Function<A, S> mapper) {

        if (bucketSeconds <= 1 || samples.isEmpty()) {
            return Collections.unmodifiableList(new ArrayList<>(samples));
        }

        Map<Long, A> buckets = new LinkedHashMap<>();
        for (S sample : samples) {
            long epochSec = tsExtractor.apply(sample).toEpochSecond(ZoneOffset.UTC);
            long bucketKey = Math.floorDiv(epochSec, bucketSeconds);
            A acc = buckets.computeIfAbsent(
                    bucketKey,
                    k -> accumulatorFactory.apply(LocalDateTime.ofEpochSecond(k * bucketSeconds, 0, ZoneOffset.UTC))
            );
            consumer.accept(acc, sample);
        }

        List<S> result = new ArrayList<>(buckets.size());
        for (A acc : buckets.values()) {
            result.add(mapper.apply(acc));
        }
        return Collections.unmodifiableList(result);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Integer maxNullable(Integer a, Integer b) {
        if (b == null) return a;
        return a == null ? b : Math.max(a, b);
    }

    private static Long maxNullableLong(Long a, Long b) {
        if (b == null) return a;
        return a == null ? b : Math.max(a, b);
    }

    private static Long sumNullableLong(Long a, Long b) {
        if (b == null) return a;
        return a == null ? b : a + b;
    }

    private static final class RangeStats {
        private int maxSamples;
        private LocalDateTime minTs;
        private LocalDateTime maxTs;

        private <T> void observe(List<T> samples, Function<T, LocalDateTime> tsExtractor) {
            if (samples.isEmpty()) {
                return;
            }
            maxSamples = Math.max(maxSamples, samples.size());
            LocalDateTime first = tsExtractor.apply(samples.get(0));
            LocalDateTime last = tsExtractor.apply(samples.get(samples.size() - 1));
            minTs = minTs == null || first.isBefore(minTs) ? first : minTs;
            maxTs = maxTs == null || last.isAfter(maxTs) ? last : maxTs;
        }
    }

    private static final class DatasourceBucketAccumulator {
        private final LocalDateTime timestamp;
        private final boolean sumWaitCount;
        private Integer inUseCount;
        private Integer activeCount;
        private Long waitCount;
        private Long maxWaitTimeMs;

        private DatasourceBucketAccumulator(LocalDateTime timestamp, boolean sumWaitCount) {
            this.timestamp = timestamp;
            this.sumWaitCount = sumWaitCount;
        }

        private void accept(DatasourcePoolSampleDto s) {
            inUseCount = maxNullable(inUseCount, s.getInUseCount());
            activeCount = maxNullable(activeCount, s.getActiveCount());
            maxWaitTimeMs = maxNullableLong(maxWaitTimeMs, s.getMaxWaitTimeMs());
            waitCount = sumWaitCount
                    ? sumNullableLong(waitCount, s.getWaitCount())
                    : maxNullableLong(waitCount, s.getWaitCount());
        }

        private DatasourcePoolSampleDto toDto() {
            return new DatasourcePoolSampleDto(
                    timestamp, inUseCount, activeCount, waitCount, maxWaitTimeMs
            );
        }
    }

    private static final class WorkerBucketAccumulator {
        private final LocalDateTime timestamp;
        private Integer poolSize;
        private Integer activeCount;
        private Integer queueSize;
        private Long completedTasks;

        private WorkerBucketAccumulator(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        private void accept(WorkerPoolSampleDto s) {
            poolSize = maxNullable(poolSize, s.getPoolSize());
            activeCount = maxNullable(activeCount, s.getActiveCount());
            queueSize = maxNullable(queueSize, s.getQueueSize());
            completedTasks = sumNullableLong(completedTasks, s.getCompletedTasks());
        }

        private WorkerPoolSampleDto toDto() {
            return new WorkerPoolSampleDto(
                    timestamp, poolSize, activeCount, queueSize, completedTasks
            );
        }
    }

    private static final class JmsBucketAccumulator {
        private final LocalDateTime timestamp;
        private Integer consumerCount;
        private Integer deliveringCount;
        private Long pendingCount;
        private Long completedMessages;
        private Long addedMessages;

        private JmsBucketAccumulator(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        private void accept(JmsQueueSampleDto s) {
            consumerCount = maxNullable(consumerCount, s.getConsumerCount());
            deliveringCount = maxNullable(deliveringCount, s.getDeliveringCount());
            pendingCount = maxNullableLong(pendingCount, s.getPendingCount());
            completedMessages = sumNullableLong(completedMessages, s.getCompletedMessages());
            addedMessages = sumNullableLong(addedMessages, s.getAddedMessages());
        }

        private JmsQueueSampleDto toDto() {
            return new JmsQueueSampleDto(
                    timestamp, consumerCount, deliveringCount, pendingCount, completedMessages, addedMessages
            );
        }
    }

    private static final class DatasourceState {
        private String poolName;
        private String jndiName;
        private boolean statisticsEnabled;
        private WaitCountType waitCountType;
        private Integer minSize;
        private Integer maxSize;
        private Integer inUseCount;
        private Integer activeCount;
        private Long waitCount;
        private Long maxWaitTimeMs;
        private Long lastRawWaitCount;
        private final Deque<DatasourcePoolSampleDto> history = new ArrayDeque<>();
    }

    private static final class WorkerPoolState {
        private String poolName;
        private String threadNamePrefix;
        private WorkerPoolType poolType;
        private Integer coreSize;
        private Integer maxSize;
        private Integer poolSize;
        private Integer activeCount;
        private Integer queueSize;
        private Integer queueCapacity;
        private Long completedTasks;
        private Long lastRawCompletedTaskCount;
        private final Deque<WorkerPoolSampleDto> history = new ArrayDeque<>();
    }

    private static final class JmsQueueState {
        private String queueName;
        private int listenerCount;
        private Integer minConsumers;
        private Integer maxConsumers;
        private Integer consumerCount;
        private Integer deliveringCount;
        private Long pendingCount;
        private Long messageCount;
        private Long completedMessages;
        private Long addedMessages;
        private Long lastRawMessagesAcknowledged;
        private Long lastRawMessagesAdded;
        private final Deque<JmsQueueSampleDto> history = new ArrayDeque<>();
    }

    private static final class JvmMemoryLayout {
        private final int alignment;
        private final int refSize;
        private final int arrayHeader;
        private final int localDateTimeTreeBytes;
        private final int datasourceSampleUpperBoundBytes;
        private final int workerSampleUpperBoundBytes;
        private final int jmsSampleUpperBoundBytes;
        private final int stateEntryOverheadBytes = 512;

        private JvmMemoryLayout(boolean compressedOops, int rawAlignment) {
            this.alignment = Math.max(8, rawAlignment);
            int objectHeader = compressedOops ? 12 : 16;
            this.refSize = compressedOops ? 4 : 8;
            this.arrayHeader = align(objectHeader + 4, this.alignment);
            int boxedInt = align(objectHeader + 4, this.alignment);
            int boxedLong = align(objectHeader + 8, this.alignment);
            this.localDateTimeTreeBytes = align(objectHeader + 8, this.alignment)
                    + align(objectHeader + 7, this.alignment)
                    + align(objectHeader + 2 * this.refSize, this.alignment);
            this.datasourceSampleUpperBoundBytes =
                    align(objectHeader + 5 * this.refSize, this.alignment) + 2 * boxedInt + 2 * boxedLong;
            this.workerSampleUpperBoundBytes =
                    align(objectHeader + 5 * this.refSize, this.alignment) + 3 * boxedInt + boxedLong;
            this.jmsSampleUpperBoundBytes =
                    align(objectHeader + 6 * this.refSize, this.alignment) + 2 * boxedInt + 3 * boxedLong;
        }

        private static JvmMemoryLayout detect() {
            boolean compressedOops = false;
            int alignment = 16;
            try {
                com.sun.management.HotSpotDiagnosticMXBean diag =
                        ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
                if (diag != null) {
                    compressedOops = Boolean.parseBoolean(diag.getVMOption("UseCompressedOops").getValue());
                    alignment = Integer.parseInt(diag.getVMOption("ObjectAlignmentInBytes").getValue());
                }
            } catch (Throwable ignored) {
                compressedOops = false;
                alignment = 16;
            }
            return new JvmMemoryLayout(compressedOops, alignment);
        }

        private long arrayDequeBackingBytes(int sampleCount) {
            int capacity = sampleCount <= 0 ? 17 : Math.max(17, Integer.highestOneBit(sampleCount) << 1);
            return align((long) arrayHeader + (long) capacity * refSize);
        }

        private int align(int size, int align) {
            return ((size + align - 1) / align) * align;
        }

        private long align(long size) {
            return ((size + alignment - 1) / alignment) * alignment;
        }
    }
}
