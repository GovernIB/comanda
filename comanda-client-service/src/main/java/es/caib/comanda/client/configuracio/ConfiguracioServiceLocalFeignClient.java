package es.caib.comanda.client.configuracio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;

@Profile(value = {"!spring-cloud & !compose"})
@FeignClient(contextId = "configuracio-client",
        name = ConfiguracioApiPath.NOM_SERVEI, url = "${es.caib.comanda.configuracio.url:localhost:8082}",
        configuration = ConfiguracioFeignClientConfig.class)
public interface ConfiguracioServiceLocalFeignClient extends ConfiguracioFeignClient {

}
