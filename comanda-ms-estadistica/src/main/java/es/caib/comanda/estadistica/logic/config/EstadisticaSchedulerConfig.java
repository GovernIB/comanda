package es.caib.comanda.estadistica.logic.config;

import es.caib.comanda.base.config.BaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Classe de configuració que habilita la funcionalitat de programació per planificar i executar tasques periòdiques
 * relacionades amb l'estadística.
 *
 * Aquesta classe utilitza les següents anotacions:
 * - `@Configuration`: Marca aquesta classe com una classe de configuració dins del context de Spring.
 * - `@EnableScheduling`: Habilita la funcionalitat de programació de tasques dins d'aquest component Spring.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableScheduling
public class EstadisticaSchedulerConfig {

    @Value("${" + BaseConfig.PROP_SCHEDULER_POOL_SIZE + ":64}")
    private Integer schedulerPoolSize;
    @Value("${" + BaseConfig.PROP_WORKER_POOL_SIZE + ":4}")
    private Integer workerPoolSize;
    @Value("${" + BaseConfig.PROP_WORKER_QUEUE_SIZE + ":200}")
    private Integer workerQueueSize;

    @Bean(name = "estadisticaTaskScheduler")
    public TaskScheduler esadisticaTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulerPoolSize);
        scheduler.setThreadNamePrefix("est-tasques-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean(name = "estadisticaWorkerExecutor")
    public TaskExecutor estadisticaWorkerExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(workerPoolSize);
        ex.setMaxPoolSize(workerPoolSize);
        ex.setQueueCapacity(workerQueueSize); // permet encolar execucions perquè no se'n perdi cap
        ex.setThreadNamePrefix("est-worker-");
        ex.initialize();
        return ex;
    }

}
