package es.caib.comanda.visualitzacio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ComandaCloudConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComandaCloudConfigApplication.class, args);
	}

}
