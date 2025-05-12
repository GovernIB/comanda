package es.caib.comanda.client.config;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració per a Open Feign.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableFeignClients(clients = {
		AppServiceClient.class,
		EntornServiceClient.class,
		EntornAppServiceClient.class,
		MonitorServiceClient.class,
		SalutServiceClient.class,
		EstadisticaServiceClient.class
})
public class OpenFeignConfig {

	@Bean
	public Retryer feignRetryer() {
		return new Retryer.Default(100, 1000, 3);
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return new CustomErrorDecoder();
	}
}
