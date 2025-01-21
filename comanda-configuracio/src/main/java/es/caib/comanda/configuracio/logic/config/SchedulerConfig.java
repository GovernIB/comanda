package es.caib.comanda.configuracio.logic.config;

import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuració de les tasques periòdiques.
 *
 * @author Limit Tecnologies
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

	@Autowired
	private AppService appService;

	@Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_APP_INFO_CRON + ":0 */1 * * * *}")
	public void appInfo() {
		appService.refreshAppInfo();
	}

}