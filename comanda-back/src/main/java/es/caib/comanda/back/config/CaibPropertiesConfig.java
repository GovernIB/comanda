package es.caib.comanda.back.config;

import es.caib.comanda.configuracio.back.controller.AppController;
import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.salut.back.controller.SalutController;
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
