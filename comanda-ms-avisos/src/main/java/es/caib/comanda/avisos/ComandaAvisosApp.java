package es.caib.comanda.avisos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-avisos.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.avisos",
		"es.caib.comanda.client" })
public class ComandaAvisosApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaAvisosApp.class, args);
	}

}
