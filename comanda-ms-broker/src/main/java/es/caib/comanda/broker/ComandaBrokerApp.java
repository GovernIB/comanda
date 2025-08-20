package es.caib.comanda.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal del microservei comanda-configuracio.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
//@ComponentScan({"es.caib.comanda.broker" })
public class ComandaBrokerApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaBrokerApp.class, args);
	}

}
