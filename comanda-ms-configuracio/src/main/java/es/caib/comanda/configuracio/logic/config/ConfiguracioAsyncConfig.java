package es.caib.comanda.configuracio.logic.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuració per habilitar l'execució asíncrona necessària per als listeners d'esdeveniments.
 */
@Configuration
@EnableAsync
public class ConfiguracioAsyncConfig {
}
