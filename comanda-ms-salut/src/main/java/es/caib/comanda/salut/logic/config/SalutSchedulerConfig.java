package es.caib.comanda.salut.logic.config;

import es.caib.comanda.base.config.BaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Configuració de les tasques periòdiques.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableScheduling
public class SalutSchedulerConfig {

//    @Value("${" + BaseConfig.PROP_SCHEDULER_POOL_SIZE + ":64}")
//    private Integer schedulerPoolSize;
    @Value("${" + BaseConfig.PROP_WORKER_POOL_SIZE + ":5}")
    private Integer workerPoolSize;
    @Value("${" + BaseConfig.PROP_WORKER_QUEUE_SIZE + ":1000}")
    private Integer workerQueueSize;

//	@Bean(name = "salutTaskScheduler")
//	public TaskScheduler salutTaskScheduler() {
//		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
//		// El scheduler només orquestra i envia feina al worker; no ha de ser el coll d'ampolla
//		scheduler.setPoolSize(schedulerPoolSize);
//		scheduler.setThreadNamePrefix("salut-tasques-");
//        scheduler.initialize();
//		return scheduler;
//	}

	@Bean(name = "salutWorkerExecutor")
	public TaskExecutor salutWorkerExecutor() {
		ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
		ex.setCorePoolSize(workerPoolSize);
		ex.setMaxPoolSize(workerPoolSize);
		ex.setQueueCapacity(workerQueueSize); // permet encolar execucions perquè no se'n perdi cap minut
		ex.setThreadNamePrefix("salut-worker-");
		ex.initialize();
		return ex;
	}
}