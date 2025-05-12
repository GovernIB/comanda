package es.caib.comanda.estadistica.logic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
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
//@Profile("!back")
@Configuration
@EnableScheduling
public class EstadisticaSchedulerConfig {

    @Bean(name = "estadisticaTaskScheduler")
    public TaskScheduler esadisticaTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // Ajusta segons les necessitats
        scheduler.setThreadNamePrefix("est-tasques-");
        return scheduler;
    }

}
