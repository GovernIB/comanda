package es.caib.comanda.alarmes.back.config;

import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.SchedulerTaskRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

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
	@Autowired
	private SchedulerTaskRegistryService schedulerTaskRegistry;

	@Value("${" + BaseConfig.PROP_SCHEDULER_ALARMES_CRON + ":30 * * * * *}")
	private String cronComprovacio;
	@Value("${" + BaseConfig.PROP_SCHEDULER_ALARMES_MAILS_AGRUPATS_CRON + ":0 0 0 * * *}")
	private String cronMailsAgrupats;

	@PostConstruct
	public void registrarTasques() {
		schedulerTaskRegistry.registerCron(
				"ALARMES_COMPROVACIO",
				"Comprovació d'alarmes",
				"Comprova l'estat de les alarmes configurades",
				cronComprovacio,
				() -> alarmaService.comprovacioScheduledTask());
		schedulerTaskRegistry.registerCron(
				"ALARMES_MAILS_AGRUPATS",
				"Enviament correus agrupats",
				"Envia els correus d'alarmes agrupats pendents",
				cronMailsAgrupats,
				() -> alarmaService.enviamentsAgrupatsScheduledTask());
	}

	@Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_ALARMES_CRON + ":30 * * * * *}")
	public void comprovacio() {
		schedulerTaskRegistry.recordStart("ALARMES_COMPROVACIO");
		try {
			alarmaService.comprovacioScheduledTask();
			schedulerTaskRegistry.recordSuccess("ALARMES_COMPROVACIO");
		} catch (Exception e) {
			schedulerTaskRegistry.recordError("ALARMES_COMPROVACIO");
			throw e;
		}
	}

	@Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_ALARMES_MAILS_AGRUPATS_CRON + ":0 0 0 * * *}")
	public void enviamentsAgrupats() {
		schedulerTaskRegistry.recordStart("ALARMES_MAILS_AGRUPATS");
		try {
			alarmaService.enviamentsAgrupatsScheduledTask();
			schedulerTaskRegistry.recordSuccess("ALARMES_MAILS_AGRUPATS");
		} catch (Exception e) {
			schedulerTaskRegistry.recordError("ALARMES_MAILS_AGRUPATS");
			throw e;
		}
	}

}
