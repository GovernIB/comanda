package es.caib.comanda.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

import javax.annotation.PostConstruct;

/**
 * Configuració del conversor per defecte d'spring per poder transformar de
 * String a Date amb el mateix format de data del JSON.
 */
@Configuration
public class ConverterConfiguration {

    @PostConstruct
    public void conversionService() {
        ((GenericConversionService) DefaultConversionService.getSharedInstance())
                .addConverter(new StringToDateConverter());
    }
}
