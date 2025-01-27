package es.caib.comanda.visualitzacio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

/**
 * Classe principal del microservei comanda-visualitzacio.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class ComandaVisualitzacioApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaVisualitzacioApp.class, args);
	}

}
