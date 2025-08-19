package es.caib.comanda.monitor.logic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuració de les tasques periòdiques.
 *
 * @author Límit Tecnologies
 */
//@Profile("!back")
@Configuration
@EnableScheduling
public class MonitorSchedulerConfig {

	@Bean(name = "monitorTaskScheduler")
	public TaskScheduler salutTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10); // Ajusta segons les necessitats
		scheduler.setThreadNamePrefix("monitor-tasques-");
		return scheduler;
	}
}