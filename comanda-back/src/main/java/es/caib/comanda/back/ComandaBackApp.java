package es.caib.comanda.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-back.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan(
		basePackages = {
				"es.caib.comanda.ms",
				"es.caib.comanda.configuracio",
				"es.caib.comanda.salut",
				"es.caib.comanda.back"
		}
)
public class ComandaBackApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaBackApp.class, args);
	}

}
