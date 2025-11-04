package es.caib.comanda.acl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal del microservei comanda-acl.
 *
 * @author Límit Tecnologies
 */
@SpringBootApplication
@ComponentScan({
		"es.caib.comanda.ms",
		"es.caib.comanda.acl",
		"es.caib.comanda.client" })
public class ComandaAclApp {

	public static void main(String[] args) {
		SpringApplication.run(ComandaAclApp.class, args);
	}

}
