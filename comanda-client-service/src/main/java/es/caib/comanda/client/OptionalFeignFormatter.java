package es.caib.comanda.client;

import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.format.FormatterRegistry;

import java.util.Optional;

//@Component
public class OptionalFeignFormatter implements FeignFormatterRegistrar {

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(
                Optional.class,
                String.class,
                optional -> {
                    if (optional.isPresent()) {
                        return optional.get().toString();
                    } else {
                        return "";
                    }
                }
        );
    }
}
