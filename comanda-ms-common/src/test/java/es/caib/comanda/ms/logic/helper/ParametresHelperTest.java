package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.client.model.Parametre;
import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import es.caib.comanda.ms.logic.intf.exception.ParametreTipusException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.support.AopUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametresHelperTest {

    @Mock
    private ParametreClientHelper parametreClientHelper;

    private ParametresHelper parametresHelper;

    private static final String AUTH_HEADER = "Bearer token";
    private static final String CODI_PARAM = "TEST_PARAM";

    @BeforeEach
    void setUp() {
        parametresHelper = new ParametresHelper(parametreClientHelper);
    }

    @Test
    @DisplayName("Troba paràmetre per codi")
    void perametreFindByCodi_quanExisteix_retornaParametre() {
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).valor("valor").build();
        when(parametreClientHelper.findByCodi(CODI_PARAM)).thenReturn(parametre);

        Parametre result = parametresHelper.perametreFindByCodi(CODI_PARAM);

        assertThat(result).isNotNull();
        assertThat(result.getCodi()).isEqualTo(CODI_PARAM);
        assertThat(result.getValor()).isEqualTo("valor");
    }

    @Test
    @DisplayName("Retorna null quan el paràmetre no existeix")
    void perametreFindByCodi_quanNoExisteix_retornaNull() {
        when(parametreClientHelper.findByCodi(CODI_PARAM)).thenReturn(null);

        Parametre result = parametresHelper.perametreFindByCodi(CODI_PARAM);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Obté paràmetre de text amb valor per defecte")
    void getParametreText_quanNoExisteix_retornaDefault() {
        when(parametreClientHelper.findByCodi(CODI_PARAM)).thenReturn(null);

        String result = parametresHelper.getParametreText(CODI_PARAM, "default");

        assertThat(result).isEqualTo("default");
    }

    @Test
    @DisplayName("Obté paràmetre booleà correctament")
    void getParametreBoolean_quanExisteix_retornaValor() {
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.BOOLEAN).valor("true").build();
        when(parametreClientHelper.findByCodi(CODI_PARAM)).thenReturn(parametre);

        Boolean result = parametresHelper.getParametreBoolean(CODI_PARAM);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Llença excepció quan el tipus no és booleà")
    void getParametreBoolean_quanTipusIncorrecte_llencaExcepcio() {
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.TEXT).valor("true").build();
        when(parametreClientHelper.findByCodi(CODI_PARAM)).thenReturn(parametre);

        assertThatThrownBy(() -> parametresHelper.getParametreBoolean(CODI_PARAM))
                .isInstanceOf(ParametreTipusException.class);
    }

    @Test
    @DisplayName("Obté paràmetre enter correctament")
    void getParametreEnter_quanExisteix_retornaValor() {
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.NUMERIC).valor("123").build();
        when(parametreClientHelper.findByCodi(CODI_PARAM)).thenReturn(parametre);

        Integer result = parametresHelper.getParametreEnter(CODI_PARAM);

        assertThat(result).isEqualTo(123);
    }

    @Test
    @DisplayName("Amb @EnableCaching actiu, tots els mètodes de ParametresHelper cachegen via ParametreClientHelper")
    void springCaching_wrappersICridaDirecta_cachegenViaClientHelper() {
        try (AnnotationConfigApplicationContext context = createCachingContext()) {
            ParametresHelper helper = context.getBean(ParametresHelper.class);
            ParametreClientHelper clientHelper = context.getBean(ParametreClientHelper.class);
            ParametreServiceClient client = context.getBean(ParametreServiceClient.class);
            HttpAuthorizationHeaderHelper authHelper = context.getBean(HttpAuthorizationHeaderHelper.class);

            assertThat(AopUtils.isAopProxy(clientHelper)).isTrue();
            when(authHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);

            String textCodi = "PARAM_TEXT";
            String numericCodi = "PARAM_NUM";
            String booleanCodi = "PARAM_BOOL";

            stubClientFind(client, textCodi, ParamTipus.TEXT, "abc");
            stubClientFind(client, numericCodi, ParamTipus.NUMERIC, "42");
            stubClientFind(client, booleanCodi, ParamTipus.BOOLEAN, "true");

            assertThat(helper.perametreFindByCodi(textCodi).getValor()).isEqualTo("abc");
            assertThat(helper.perametreFindByCodi(textCodi, "def").getValor()).isEqualTo("abc");
            assertThat(helper.getParametreText(textCodi)).isEqualTo("abc");
            assertThat(helper.getParametreText(textCodi, "def")).isEqualTo("abc");

            assertThat(helper.getParametreEnter(numericCodi)).isEqualTo(42);
            assertThat(helper.getParametreEnter(numericCodi, 0)).isEqualTo(42);
            assertThat(helper.getParametreNumeric(numericCodi)).isEqualTo(42.0);
            assertThat(helper.getParametreNumeric(numericCodi, 0.0)).isEqualTo(42.0);

            assertThat(helper.getParametreBoolean(booleanCodi)).isTrue();
            assertThat(helper.getParametreBoolean(booleanCodi, false)).isTrue();

            verify(client, times(1)).find(isNull(), eq("codi:'" + textCodi + "'"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(AUTH_HEADER));
            verify(client, times(1)).find(isNull(), eq("codi:'" + numericCodi + "'"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(AUTH_HEADER));
            verify(client, times(1)).find(isNull(), eq("codi:'" + booleanCodi + "'"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(AUTH_HEADER));
            verify(authHelper, times(3)).getAuthorizationHeader();
        }
    }

    @Test
    @DisplayName("Quan el client llança FeignException, retorna el valor per defecte, no cacheja l'error i reintenta")
    void springCaching_quanClientLlancaFeignException_noCachejaErrorIReintenta() {
        try (AnnotationConfigApplicationContext context = createCachingContext()) {
            ParametresHelper helper = context.getBean(ParametresHelper.class);
            ParametreServiceClient client = context.getBean(ParametreServiceClient.class);
            HttpAuthorizationHeaderHelper authHelper = context.getBean(HttpAuthorizationHeaderHelper.class);

            when(authHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
            when(client.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenThrow(mock(FeignException.class))
                    .thenReturn(pagedOf(Parametre.builder().codi(CODI_PARAM).tipus(ParamTipus.TEXT).valor("recuperat").build()));

            assertThat(helper.getParametreText(CODI_PARAM, "fallback")).isEqualTo("fallback");
            assertThat(helper.getParametreText(CODI_PARAM, "fallback")).isEqualTo("recuperat");
            assertThat(helper.getParametreText(CODI_PARAM, "fallback")).isEqualTo("recuperat");

            verify(client, times(2)).find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER));
        }
    }

    private static AnnotationConfigApplicationContext createCachingContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getDefaultListableBeanFactory().setAllowCircularReferences(false);
        context.register(CachingTestConfig.class, ParametreClientHelper.class, ParametresHelper.class);
        context.refresh();
        return context;
    }

    private static void stubClientFind(ParametreServiceClient client, String codi, ParamTipus tipus, String valor) {
        Parametre parametre = Parametre.builder().codi(codi).tipus(tipus).valor(valor).build();
        when(client.find(isNull(), eq("codi:'" + codi + "'"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(AUTH_HEADER)))
                .thenReturn(pagedOf(parametre));
    }

    private static PagedModel<EntityModel<Parametre>> pagedOf(Parametre parametre) {
        return PagedModel.of(
                Collections.singletonList(EntityModel.of(parametre)),
                new PagedModel.PageMetadata(1, 0, 1));
    }

    @Configuration
    @EnableCaching
    static class CachingTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(HazelCastCacheConfig.PARAMETRE_CACHE);
        }

        @Bean
        ParametreServiceClient parametreServiceClient() {
            return mock(ParametreServiceClient.class);
        }

        @Bean
        HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper() {
            return mock(HttpAuthorizationHeaderHelper.class);
        }
    }
}
