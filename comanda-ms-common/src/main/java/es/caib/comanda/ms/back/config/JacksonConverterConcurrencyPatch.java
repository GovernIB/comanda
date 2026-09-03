package es.caib.comanda.ms.back.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Patch for Spring Framework issue #36090.
 * <p>
 * Prevents ConcurrentModificationException and ArrayIndexOutOfBoundsException in
 * AbstractJackson2HttpMessageConverter by replacing the non-thread-safe
 * objectMapperRegistrations LinkedHashMap with a fully thread-safe ConcurrentHashMap.
 * <p>
 * Context: Spring Cloud OpenFeign lazily spins up child ApplicationContexts upon the
 * first invocation of a @FeignClient. If multiple Feign clients are initialized
 * concurrently on different request threads, Spring HATEOAS auto-configurations inside
 * those child contexts will attempt to configure the shared HttpMessageConverters
 * inherited from the parent context. This results in concurrent writes to the
 * converter's LinkedHashMap, permanently corrupting its internal node structure.
 * <p>
 * This BeanPostProcessor intercepts the converters at boot and makes them thread-safe,
 * immunizing the application against these lazy-initialization race conditions.
 */
@Component
public class JacksonConverterConcurrencyPatch implements BeanPostProcessor {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonConverterConcurrencyPatch.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof AbstractJackson2HttpMessageConverter) {
            applyPatch((AbstractJackson2HttpMessageConverter) bean);
        } else if (bean instanceof HttpMessageConverters) {
            for (HttpMessageConverter<?> converter : ((HttpMessageConverters) bean).getConverters()) {
                if (converter instanceof AbstractJackson2HttpMessageConverter) {
                    applyPatch((AbstractJackson2HttpMessageConverter) converter);
                }
            }
        } else if (bean instanceof RestTemplate) {
            for (HttpMessageConverter<?> converter : ((RestTemplate) bean).getMessageConverters()) {
                if (converter instanceof AbstractJackson2HttpMessageConverter) {
                    applyPatch((AbstractJackson2HttpMessageConverter) converter);
                }
            }
        }
        return bean;
    }

    private void applyPatch(AbstractJackson2HttpMessageConverter converter) {
        try {
            Field field = AbstractJackson2HttpMessageConverter.class.getDeclaredField("objectMapperRegistrations");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<Class<?>, Map<MediaType, ObjectMapper>> existing =
                    (Map<Class<?>, Map<MediaType, ObjectMapper>>) field.get(converter);

            ConcurrentHashMap<Class<?>, Map<MediaType, ObjectMapper>> threadSafeMap =
                    new ConcurrentHashMap<Class<?>, Map<MediaType, ObjectMapper>>() {
                        @Override
                        public Map<MediaType, ObjectMapper> computeIfAbsent(Class<?> key, Function<? super Class<?>, ? extends Map<MediaType, ObjectMapper>> mappingFunction) {
                            return super.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
                        }
                    };

            if (existing != null) {
                for (Map.Entry<Class<?>, Map<MediaType, ObjectMapper>> entry : existing.entrySet()) {
                    threadSafeMap.put(entry.getKey(), new ConcurrentHashMap<>(entry.getValue()));
                }
            }

            field.set(converter, threadSafeMap);
        } catch (Exception e) {
            log.warn("Failed to apply concurrency patch to AbstractJackson2HttpMessageConverter. " +
                     "The application may be vulnerable to Spring Issue #36090. Reason: {}", e.getMessage());
        }
    }
}
