package es.caib.comanda.salut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-salut.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({ "es.caib.comanda.ms", "es.caib.comanda.salut" })
public class ComandaSalutApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaSalutApp.class, args);
	}

}
