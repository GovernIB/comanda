package es.caib.comanda.client;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració per a Open Feign.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableFeignClients(clients = {
		AppServiceClient.class
})
public class OpenFeignConfig {

}
