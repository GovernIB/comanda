package es.caib.comanda.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-ms-monitor.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.monitor",
		"es.caib.comanda.client" })
public class ComandaMonitorApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaMonitorApp.class, args);
	}

}
