package es.caib.comanda.alarmes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-alarmes.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.alarmes",
		"es.caib.comanda.client" })
public class ComandaAlarmesApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaAlarmesApp.class, args);
	}

}
