package es.caib.comanda.back;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Classe principal del microservei comanda-back.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan(
		basePackages = {
				"es.caib.comanda.ms",
				"es.caib.comanda.configuracio",
				"es.caib.comanda.salut",
				"es.caib.comanda.back"
		},
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.REGEX,
				pattern = {
						"es\\.caib\\.comanda\\.configuracio\\.back\\.config\\..*",
						"es\\.caib\\.comanda\\.configuracio\\.persist\\.config\\..*",
						"es\\.caib\\.comanda\\.salut\\.back\\.config\\..*",
						"es\\.caib\\.comanda\\.salut\\.persist\\.config\\..*"
				})
)
public class ComandaBackApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaBackApp.class, args);
	}

}
