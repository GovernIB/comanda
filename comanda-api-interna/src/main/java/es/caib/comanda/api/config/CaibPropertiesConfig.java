package es.caib.comanda.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWarDeployment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@ConditionalOnWarDeployment
@PropertySource("classpath:/caib-application.properties")
public class CaibPropertiesConfig {

}
