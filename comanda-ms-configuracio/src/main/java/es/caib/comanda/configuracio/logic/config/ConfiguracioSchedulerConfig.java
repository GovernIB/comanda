package es.caib.comanda.configuracio.logic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuració de les tasques periòdiques.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableScheduling
public class ConfiguracioSchedulerConfig {

	@Primary
	@Bean(name = "configuracioTaskScheduler")
	public TaskScheduler configuracioTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10); // Ajusta segons les necessitats
		scheduler.setThreadNamePrefix("app-tasques-");
		return scheduler;
	}

}