package es.caib.comanda.configuracio.logic.config;

import es.caib.comanda.base.config.BaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuració de les tasques periòdiques.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableScheduling
public class ConfiguracioSchedulerConfig {

    @Value("${" + BaseConfig.PROP_WORKER_POOL_SIZE + ":4}")
    private Integer workerPoolSize;
    @Value("${" + BaseConfig.PROP_WORKER_QUEUE_SIZE + ":200}")
    private Integer workerQueueSize;

    @Bean(name = "configuracioWorkerExecutor")
    public TaskExecutor configuracioWorkerExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(workerPoolSize);
        ex.setMaxPoolSize(workerPoolSize);
        ex.setQueueCapacity(workerQueueSize); // permet encolar execucions perquè no se'n perdi cap
        ex.setThreadNamePrefix("conf-worker-");
        ex.initialize();
        return ex;
    }

}