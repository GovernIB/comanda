package es.caib.comanda.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWarDeployment;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@ConditionalOnWarDeployment
//@SpringBootApplication
public class ComandaGatewayApplication extends SpringBootServletInitializer {

//	public static void main(String[] args) {
//		SpringApplication.run(ComandaGatewayApplication.class, args);
//	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ComandaGatewayApplication.class);
	}
}
