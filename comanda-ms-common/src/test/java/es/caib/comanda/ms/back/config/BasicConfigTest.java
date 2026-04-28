package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.back.intf.HandlerInterceptorWithPath;
import es.caib.comanda.ms.logic.intf.model.UnpagedButSorted;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BasicConfigTest {

    @Test
    void baseMessageSourceConfig_createsConfiguredMessageSource() {
        // Comprova que la configuració de missatges crea un message source correctament inicialitzat.
        BaseMessageSourceConfig config = new BaseMessageSourceConfig() {};
        MessageSource messageSource = config.messageSource();

        assertThat(config.getBasename()).isEqualTo("comanda-messages");
        assertThat(messageSource).isInstanceOf(ReloadableResourceBundleMessageSource.class);
    }

    @Test
    void corsAndMetricsConfig_createBeans() {
        // Verifica la creació dels beans de CORS i mètriques bàsics.
        assertThat(new CorsConfig().corsFilter()).isNotNull();
        assertThat(new MetricsConfig().meterRegistry()).isInstanceOf(SimpleMeterRegistry.class);
    }

    @Test
    void webMvcConfig_coversAsyncInterceptorsResolversAndResources() throws Exception {
        // Exercita la configuració MVC: asíncronia, interceptors, resolvers i recursos estàtics.
        HandlerInterceptorWithPath interceptor = new HandlerInterceptorWithPath() {
            @Override public String getPath() { return "/api/**"; }
        };
        WebMvcConfig config = new WebMvcConfig(List.of(interceptor));
        org.springframework.test.util.ReflectionTestUtils.setField(config, "workerPoolSize", 2);
        org.springframework.test.util.ReflectionTestUtils.setField(config, "workerQueueSize", 5);

        var executor = config.mvcAsyncTaskExecutor();
        assertThat(executor).isNotNull();

        AsyncSupportConfigurer asyncSupportConfigurer = new AsyncSupportConfigurer();
        config.configureAsyncSupport(asyncSupportConfigurer);

        InterceptorRegistry interceptorRegistry = new InterceptorRegistry();
        config.addInterceptors(interceptorRegistry);

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
        config.addArgumentResolvers(resolvers);
        assertThat(resolvers).isNotEmpty();

        ResourceHandlerRegistry resourceRegistry = new ResourceHandlerRegistry(mock(ApplicationContext.class), new MockServletContext());
        config.addResourceHandlers(resourceRegistry);
    }

    @Test
    void customPageableResolver_supportsNullUnpagedAndPaged() throws Exception {
        // Comprova que el resolver de Pageable suporta valors nuls, unpaged i paged.
        WebMvcConfig.CustomPageableHandlerMethodArgumentResolver resolver = new WebMvcConfig.CustomPageableHandlerMethodArgumentResolver();
        Method method = BasicConfigTest.class.getDeclaredMethod("pageableMethod", org.springframework.data.domain.Pageable.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertThat(resolver.supportsParameter(parameter)).isTrue();

        MockHttpServletRequest req = new MockHttpServletRequest();
        assertThat(resolver.resolveArgument(parameter, null, new ServletWebRequest(req), null)).isNull();

        req = new MockHttpServletRequest();
        req.addParameter("page", "UNPAGED");
        req.addParameter("sort", "name,asc");
        assertThat(resolver.resolveArgument(parameter, null, new ServletWebRequest(req), null)).isInstanceOf(UnpagedButSorted.class);

        req = new MockHttpServletRequest();
        req.addParameter("page", "2");
        req.addParameter("size", "5");
        req.addParameter("sort", "name,desc");
        org.springframework.data.domain.Pageable pageable = resolver.resolveArgument(parameter, null, new ServletWebRequest(req), null);
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().isSorted()).isTrue();
    }

    @SuppressWarnings("unused")
    private void pageableMethod(org.springframework.data.domain.Pageable pageable) {
    }
}
