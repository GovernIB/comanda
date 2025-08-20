package es.caib.comanda.usuaris;

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
		"es.caib.comanda.usuaris",
		"es.caib.comanda.client" })
public class ComandaUsuarisApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaUsuarisApp.class, args);
	}

}
