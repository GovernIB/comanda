package es.caib.comanda.client.configuracio;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;

@Profile(value = {"spring-cloud", "compose"})
@FeignClient(contextId = "configuracio-client",
        name = ConfiguracioApiPath.NOM_SERVEI,
        configuration = ConfiguracioFeignClientConfig.class)
public interface ConfiguracioServiceCloudFeignClient extends ConfiguracioFeignClient {

}
