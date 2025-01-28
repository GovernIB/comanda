package es.caib.comanda.configuracio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-configuracio.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({ "es.caib.comanda.ms", "es.caib.comanda.configuracio" })
public class ComandaConfiguracioApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaConfiguracioApp.class, args);
	}

}
