package es.caib.comanda.tasques;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-configuracio.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.tasques",
		"es.caib.comanda.client" })
public class ComandaTasquesApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaTasquesApp.class, args);
	}

}
