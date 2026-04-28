package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.logic.intf.service.PermissionEvaluatorService;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OpenApiAndSecurityConfigTest {

    @Test
    void baseOpenApiConfig_buildsBearerBasicAndNoAuth() {
        // Verifica la generació de l'OpenAPI per als modes bearer, basic i sense autenticació.
        OpenAPI bearer = new TestOpenApiConfig(true, BaseOpenApiConfig.AuthType.BEARER).customOpenAPI();
        OpenAPI basic = new TestOpenApiConfig(true, BaseOpenApiConfig.AuthType.BASIC).customOpenAPI();
        OpenAPI none = new TestOpenApiConfig(false, BaseOpenApiConfig.AuthType.BEARER).customOpenAPI();

        assertThat(bearer.getComponents()).isNotNull();
        assertThat(bearer.getSecurity()).isNotEmpty();
        assertThat(basic.getComponents().getSecuritySchemes()).containsKey(BaseOpenApiConfig.SECURITY_NAME);
        assertThat(none.getComponents()).isNull();
    }

    @Test
    void methodSecurityConfig_createsExpressionHandler() {
        // Comprova que la configuració de seguretat de mètode crea l'expression handler esperat.
        MethodSecurityConfig config = new MethodSecurityConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(config, "applicationContext", mock(ApplicationContext.class));
        org.springframework.test.util.ReflectionTestUtils.setField(config, "permissionEvaluatorService", mock(PermissionEvaluatorService.class));

        MethodSecurityExpressionHandler handler = config.createExpressionHandler();

        assertThat(handler).isNotNull();
    }

    @Test
    void hateoasMessageResolverConfig_createsResolver() {
        // Verifica la creació del resolver de missatges per HATEOAS.
        TestHateoasConfig config = new TestHateoasConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(config, "context", new StaticApplicationContext());
        MessageResolver resolver = config.customMessageResolver();
        assertThat(resolver).isNotNull();
    }

    static class TestOpenApiConfig extends BaseOpenApiConfig {
        private final boolean auth;
        private final AuthType authType;

        TestOpenApiConfig(boolean auth, AuthType authType) {
            this.auth = auth;
            this.authType = authType;
        }

        @Override
        protected String getTitle() {
            return "Test API";
        }

        @Override
        protected boolean enableAuthComponent() {
            return auth;
        }

        @Override
        protected AuthType getAuthType() {
            return authType;
        }
    }

    static class TestHateoasConfig extends BaseHateoasMessageResolverConfig {
        @Override
        protected String getCommonBasename() {
            return null;
        }

        @Override
        protected String getBasename() {
            return "comanda-rest-messages";
        }
    }
}
