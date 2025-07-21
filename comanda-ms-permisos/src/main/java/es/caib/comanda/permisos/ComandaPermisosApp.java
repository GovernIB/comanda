package es.caib.comanda.permisos;

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
		"es.caib.comanda.permisos",
		"es.caib.comanda.client" })
public class ComandaPermisosApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaPermisosApp.class, args);
	}

}
