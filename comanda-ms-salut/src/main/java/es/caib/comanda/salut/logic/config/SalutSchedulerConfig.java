package es.caib.comanda.salut.logic.config;

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
@Configuration
@EnableScheduling
public class SalutSchedulerConfig {

	@Bean(name = "salutTaskScheduler")
	public TaskScheduler salutTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10); // Ajusta segons les necessitats
		scheduler.setThreadNamePrefix("salut-tasques-");
		return scheduler;
	}

}