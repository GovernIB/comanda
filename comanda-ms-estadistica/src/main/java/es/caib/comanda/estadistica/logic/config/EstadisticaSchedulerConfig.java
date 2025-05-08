package es.caib.comanda.estadistica.logic.config;

import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Classe de configuració que habilita la funcionalitat de programació per planificar i executar tasques periòdiques
 * relacionades amb l'estadística.
 *
 * Aquesta classe utilitza les següents anotacions:
 * - `@Configuration`: Marca aquesta classe com una classe de configuració dins del context de Spring.
 * - `@EnableScheduling`: Habilita la funcionalitat de programació de tasques dins d'aquest component Spring.
 * - `@RequiredArgsConstructor`: Genera al constructor els arguments necessaris per a la injecció de dependències,
 * que en aquest cas és el servei FetService.
 *
 * @author Límit Tecnologies
 */
@RequiredArgsConstructor
@Configuration
@EnableScheduling
public class EstadisticaSchedulerConfig {

    private final FetService fetService;

    /**
     * Mètode programat que executa periòdicament la recuperació d'informació estadística de totes les aplicacions actives.
     *
     * Aquest mètode fa ús de l'anotació @Scheduled per establir un cronograma d'execució basat en la propietat
     * `scheduler.estadistiques.info.cron`, configurada dins del fitxer de propietats de l'aplicació.
     *
     * La crida es delega al servei `fetService`, que implementa la funcionalitat de recuperació de dades estadístiques
     * mitjançant el mètode `getEstadisticaInfo`.
     *
     * Cal assegurar-se que la propietat `scheduler.estadistiques.info.cron` està configurada adequadament per definir l'interval d'execució.
     */
    @Scheduled(cron = "${" + BaseConfig.PROP_SCHEDULER_ESTADISTIQUES_INFO_CRON + ":1 0 * * * *}")
    public void getEstadistiques() {
        fetService.getEstadisticaInfo();
    }

}
