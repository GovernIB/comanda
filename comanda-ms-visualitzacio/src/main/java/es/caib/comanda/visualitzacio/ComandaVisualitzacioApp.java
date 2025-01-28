package es.caib.comanda.visualitzacio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-visualitzacio.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.visualitzacio" })
public class ComandaVisualitzacioApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaVisualitzacioApp.class, args);
	}

}
