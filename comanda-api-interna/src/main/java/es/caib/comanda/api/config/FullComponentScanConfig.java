package es.caib.comanda.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@Profile("!openapi")  // S'activa sempre excepte quan el perfil és "openapi"
@Configuration
@ComponentScan(
        basePackages = {
                "es.caib.comanda.ms",
                "es.caib.comanda.configuracio",
                "es.caib.comanda.salut",
                "es.caib.comanda.estadistica",
                "es.caib.comanda.monitor",
                "es.caib.comanda.api",
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = {
                        "es\\.caib\\.comanda\\.configuracio\\.back\\.config\\..*",
                        "es\\.caib\\.comanda\\.configuracio\\.persist\\.config\\..*",
                        "es\\.caib\\.comanda\\.salut\\.back\\.config\\..*",
                        "es\\.caib\\.comanda\\.salut\\.persist\\.config\\..*",
                        "es\\.caib\\.comanda\\.estadistica\\.back\\.config\\..*",
                        "es\\.caib\\.comanda\\.estadistica\\.persist\\.config\\..*",
                        "es\\.caib\\.comanda\\.monitor\\.back\\.config\\..*",
                        "es\\.caib\\.comanda\\.monitor\\.persist\\.config\\..*"
                })
)
public class FullComponentScanConfig {
}
