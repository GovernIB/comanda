package es.caib.comanda.salut.logic.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.salut.logic.intf.service.SalutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuració de les tasques periòdiques.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableScheduling
public class SalutSchedulerConfig {

	@Autowired
	private SalutService salutService;

	@Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_SALUT_INFO_CRON + ":0 */1 * * * *}")
	public void salutInfo() {
		salutService.getSalutInfo();
	}

}