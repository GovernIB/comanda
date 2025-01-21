package es.caib.comanda.configuracio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * Classe principal del microservei comanda-configuracio.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({ "es.caib.comanda.ms", "es.caib.comanda.configuracio" })
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class ComandaConfiguracioApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaConfiguracioApp.class, args);
	}

}
