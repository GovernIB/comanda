package es.caib.comanda.estadistica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-salut.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.estadistica",
		"es.caib.comanda.client" })
public class ComandaEstadisticaApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaEstadisticaApp.class, args);
	}

}
