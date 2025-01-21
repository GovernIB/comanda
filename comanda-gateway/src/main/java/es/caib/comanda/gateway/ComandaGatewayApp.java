package es.caib.comanda.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * Classe principal del gateway per comanda.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class ComandaGatewayApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaGatewayApp.class, args);
	}

}
