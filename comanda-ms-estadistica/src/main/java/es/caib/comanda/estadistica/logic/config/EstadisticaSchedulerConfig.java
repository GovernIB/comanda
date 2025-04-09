package es.caib.comanda.estadistica.logic.config;

import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
@Configuration
@EnableScheduling
public class EstadisticaSchedulerConfig {

    private final FetService fetService;

    @Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_ESTADISTIQUES_INFO_CRON + ":1 0 * * * *}")
    public void getEstadistiques() {

    }

}
