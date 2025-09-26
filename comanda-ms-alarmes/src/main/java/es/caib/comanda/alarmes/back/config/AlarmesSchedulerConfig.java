package es.caib.comanda.alarmes.back.config;

import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.base.config.BaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuració de les tasques periòdiques del ms d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableScheduling
public class AlarmesSchedulerConfig {

	@Autowired
	private AlarmaService alarmaService;

	@Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_ALARMES_CRON + ":30 * * * * *}")
	public void comprovacio() {
		alarmaService.comprovacioScheduledTask();
	}

}