package es.caib.comanda.client.config;

import es.caib.comanda.client.CustomQueryMapEncoder;
import feign.QueryMapEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryMapperConfig {

    @Bean
    public QueryMapEncoder queryMapEncoder() {
        return new CustomQueryMapEncoder();
    }
}
