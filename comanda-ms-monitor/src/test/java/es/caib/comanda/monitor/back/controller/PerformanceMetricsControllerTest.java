package es.caib.comanda.monitor.back.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.monitor.logic.intf.model.performance.DatasourcePoolMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.DatasourcePoolSampleDto;
import es.caib.comanda.monitor.logic.intf.model.performance.JmsQueueMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.JmsQueueSampleDto;
import es.caib.comanda.monitor.logic.intf.model.performance.PerformanceMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WaitCountType;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolMetricsDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolSampleDto;
import es.caib.comanda.monitor.logic.intf.model.performance.WorkerPoolType;
import es.caib.comanda.monitor.logic.service.PerformanceMetricsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a PerformanceMetricsController")
class PerformanceMetricsControllerTest {

    @Mock
    private PerformanceMetricsServiceImpl service;

    @InjectMocks
    private PerformanceMetricsController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/performance-metrics sense paràmetres passa nulls al servei i serialitza camps no nuls")
    void getMetrics_senseParametres_retornaDtoSerialitzatCorrectament() throws Exception {
        DatasourcePoolMetricsDto enabled = new DatasourcePoolMetricsDto();
        enabled.setPoolName("comandaDB");
        enabled.setJndiName("java:jboss/datasources/comandaDS");
        enabled.setStatisticsEnabled(true);
        enabled.setWaitCountType(WaitCountType.INTERVAL_DELTA);
        enabled.setMinSize(5);
        enabled.setMaxSize(50);
        enabled.setInUseCount(2);
        enabled.setActiveCount(10);
        enabled.setWaitCount(0L);
        enabled.setMaxWaitTimeMs(15L);

        DatasourcePoolMetricsDto disabled = new DatasourcePoolMetricsDto();
        disabled.setPoolName("ExampleDS");
        disabled.setJndiName("java:jboss/datasources/ExampleDS");
        disabled.setStatisticsEnabled(false);
        disabled.setWaitCountType(WaitCountType.INTERVAL_DELTA);
        disabled.setMinSize(1);
        disabled.setMaxSize(20);

        LocalDateTime now = LocalDateTime.of(2026, 11, 19, 12, 0, 0);

        WorkerPoolMetricsDto worker = new WorkerPoolMetricsDto();
        worker.setPoolName("salutWorkerExecutor");
        worker.setThreadNamePrefix("salut-worker-");
        worker.setPoolType(WorkerPoolType.EXECUTOR);
        worker.setCoreSize(5);
        worker.setMaxSize(5);
        worker.setPoolSize(5);
        worker.setActiveCount(2);
        worker.setQueueSize(1);
        worker.setQueueCapacity(1000);
        worker.setCompletedTasks(4L);
        worker.setHistory(List.of(new WorkerPoolSampleDto(now, 5, 2, 1, 4L)));

        JmsQueueMetricsDto jms = new JmsQueueMetricsDto();
        jms.setQueueName("tasquesQueue");
        jms.setListenerCount(1);
        jms.setMinConsumers(5);
        jms.setMaxConsumers(50);
        jms.setConsumerCount(5);
        jms.setDeliveringCount(1);
        jms.setPendingCount(2L);
        jms.setMessageCount(3L);
        jms.setCompletedMessages(7L);
        jms.setAddedMessages(8L);
        jms.setHistory(List.of(new JmsQueueSampleDto(now, 5, 1, 2L, 7L, 8L)));

        PerformanceMetricsDto responseDto = new PerformanceMetricsDto(
                now,
                1,
                3600,
                300,
                2,
                2,
                true,
                4096L,
                now.minusSeconds(300),
                now,
                now.minusSeconds(300),
                now,
                List.of(enabled, disabled),
                List.of(worker),
                List.of(jms)
        );
        when(service.getMetrics(isNull(), isNull(), isNull(), isNull())).thenReturn(responseDto);

        mockMvc.perform(get(BaseConfig.API_PATH + "/performance-metrics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.samplingIntervalSeconds").value(1))
                .andExpect(jsonPath("$.maxHistoryPoints").value(3600))
                .andExpect(jsonPath("$.windowSeconds").value(300))
                .andExpect(jsonPath("$.bucketSeconds").value(2))
                .andExpect(jsonPath("$.collectionEnabled").value(true))
                .andExpect(jsonPath("$.estimatedMemoryBytes").value(4096))
                .andExpect(jsonPath("$.datasources", hasSize(2)))
                .andExpect(jsonPath("$.datasources[0].poolName").value("comandaDB"))
                .andExpect(jsonPath("$.datasources[0].statisticsEnabled").value(true))
                .andExpect(jsonPath("$.datasources[0].waitCountType").value("INTERVAL_DELTA"))
                .andExpect(jsonPath("$.datasources[0].history").isArray())
                .andExpect(jsonPath("$.datasources[0].preview").doesNotExist())
                .andExpect(jsonPath("$.datasources[1].poolName").value("ExampleDS"))
                .andExpect(jsonPath("$.datasources[1].statisticsEnabled").value(false))
                .andExpect(jsonPath("$.datasources[1].history").isArray())
                .andExpect(jsonPath("$.workerPools", hasSize(1)))
                .andExpect(jsonPath("$.workerPools[0].poolName").value("salutWorkerExecutor"))
                .andExpect(jsonPath("$.workerPools[0].poolType").value("EXECUTOR"))
                .andExpect(jsonPath("$.workerPools[0].completedTasks").value(4))
                .andExpect(jsonPath("$.workerPools[0].history", hasSize(1)))
                .andExpect(jsonPath("$.jmsQueues", hasSize(1)))
                .andExpect(jsonPath("$.jmsQueues[0].queueName").value("tasquesQueue"))
                .andExpect(jsonPath("$.jmsQueues[0].completedMessages").value(7))
                .andExpect(jsonPath("$.jmsQueues[0].addedMessages").value(8))
                .andExpect(jsonPath("$.jmsQueues[0].history", hasSize(1)));

        verify(service).getMetrics(null, null, null, null);
    }

    @Test
    @DisplayName("GET /api/performance-metrics amb finestra i rang de zoom reenvia windowSeconds, from, to i maxPoints")
    void getMetrics_ambParametresZoom_reenviaAlServeiISerialitzaPreview() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 11, 19, 12, 0, 0);
        LocalDateTime from = LocalDateTime.of(2026, 11, 19, 11, 30, 0);
        LocalDateTime to = LocalDateTime.of(2026, 11, 19, 11, 35, 0);

        DatasourcePoolMetricsDto ds = new DatasourcePoolMetricsDto();
        ds.setPoolName("comandaDB");
        ds.setStatisticsEnabled(true);
        ds.setWaitCountType(WaitCountType.INTERVAL_DELTA);
        ds.setHistory(List.of(new DatasourcePoolSampleDto(from, 10, 15, 2L, 30L)));
        ds.setPreview(List.of(new DatasourcePoolSampleDto(now.minusHours(1), 10, 15, 2L, 30L)));

        PerformanceMetricsDto responseDto = new PerformanceMetricsDto(
                now, 1, 3600, 3600, 2, 20, true, 2048L, now.minusHours(1), now, from, to,
                List.of(ds), Collections.emptyList(), Collections.emptyList()
        );
        when(service.getMetrics(3600, from, to, 180)).thenReturn(responseDto);

        mockMvc.perform(get(BaseConfig.API_PATH + "/performance-metrics")
                        .param("windowSeconds", "3600")
                        .param("from", "2026-11-19T11:30:00")
                        .param("to", "2026-11-19T11:35:00")
                        .param("maxPoints", "180")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.windowSeconds").value(3600))
                .andExpect(jsonPath("$.bucketSeconds").value(2))
                .andExpect(jsonPath("$.previewBucketSeconds").value(20))
                .andExpect(jsonPath("$.datasources[0].history", hasSize(1)))
                .andExpect(jsonPath("$.datasources[0].preview", hasSize(1)))
                .andExpect(jsonPath("$.workerPools", hasSize(0)))
                .andExpect(jsonPath("$.jmsQueues", hasSize(0)));

        verify(service).getMetrics(3600, from, to, 180);
    }

    @Test
    @DisplayName("GET /api/performance-metrics serialitza col·leccions buides quan no hi ha pools detectats")
    void getMetrics_senseDatasources_serialitzaArrayBuit() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        PerformanceMetricsDto emptyDto = new PerformanceMetricsDto(
                now, 1, 3600, 300, 1, 1, true, 0L, now.minusSeconds(300), now, now.minusSeconds(300), now,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()
        );
        when(service.getMetrics(null, null, null, null)).thenReturn(emptyDto);

        mockMvc.perform(get(BaseConfig.API_PATH + "/performance-metrics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasources", hasSize(0)))
                .andExpect(jsonPath("$.workerPools", hasSize(0)))
                .andExpect(jsonPath("$.jmsQueues", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/performance-metrics/export retorna JSON complet amb capçalera Content-Disposition attachment")
    void exportMetrics_retornaJsonAmbCapcaleraAttachment() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 11, 19, 12, 0, 0);
        DatasourcePoolMetricsDto ds = new DatasourcePoolMetricsDto();
        ds.setPoolName("comandaDB");
        ds.setStatisticsEnabled(true);
        ds.setWaitCountType(WaitCountType.INTERVAL_DELTA);
        ds.setHistory(List.of(new DatasourcePoolSampleDto(now, 2, 10, 0L, 12L)));

        WorkerPoolMetricsDto wp = new WorkerPoolMetricsDto();
        wp.setPoolName("salutWorkerExecutor");
        wp.setPoolType(WorkerPoolType.EXECUTOR);
        wp.setHistory(List.of(new WorkerPoolSampleDto(now, 5, 2, 1, 4L)));

        JmsQueueMetricsDto jms = new JmsQueueMetricsDto();
        jms.setQueueName("tasquesQueue");
        jms.setHistory(List.of(new JmsQueueSampleDto(now, 5, 1, 2L, 7L, 8L)));

        PerformanceMetricsDto snapshot = new PerformanceMetricsDto(
                now, 1, 3600, 120, 1, 1, true, 4096L, now.minusSeconds(120), now, now.minusSeconds(120), now,
                List.of(ds), List.of(wp), List.of(jms)
        );
        when(service.exportHistory()).thenReturn(snapshot);

        mockMvc.perform(get(BaseConfig.API_PATH + "/performance-metrics/export")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"performance-metrics-20261119-120000.json\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bucketSeconds").value(1))
                .andExpect(jsonPath("$.collectionEnabled").value(true))
                .andExpect(jsonPath("$.estimatedMemoryBytes").value(4096))
                .andExpect(jsonPath("$.datasources", hasSize(1)))
                .andExpect(jsonPath("$.workerPools", hasSize(1)))
                .andExpect(jsonPath("$.jmsQueues", hasSize(1)));

        verify(service).exportHistory();
    }
}
