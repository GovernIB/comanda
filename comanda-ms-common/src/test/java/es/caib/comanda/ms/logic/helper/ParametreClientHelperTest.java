package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.client.model.Parametre;
import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.support.AopUtils;
import org.springframework.cache.Cache;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametreClientHelperTest {

    @Mock
    private ParametreServiceClient parametreServiceClient;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    private ParametreClientHelper parametreClientHelper;

    private static final String AUTH_HEADER = "Bearer token";
    private static final String CODI_PARAM = "TEST_PARAM";

    @BeforeEach
    void setUp() {
        parametreClientHelper = new ParametreClientHelper(parametreServiceClient, httpAuthorizationHeaderHelper);
    }

    @Test
    @DisplayName("Troba paràmetre per codi")
    void findByCodi_quanExisteix_retornaParametre() {
        Parametre parametre = Parametre.builder().codi(CODI_PARAM).valor("valor").build();
        PagedModel<EntityModel<Parametre>> pagedModel = pagedOf(parametre);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        Parametre result = parametreClientHelper.findByCodi(CODI_PARAM);

        assertThat(result).isNotNull();
        assertThat(result.getCodi()).isEqualTo(CODI_PARAM);
        assertThat(result.getValor()).isEqualTo("valor");
    }

    @Test
    @DisplayName("Retorna null quan el paràmetre no existeix")
    void findByCodi_quanNoExisteix_retornaNull() {
        PagedModel<EntityModel<Parametre>> pagedModel = PagedModel.of(
                Collections.emptyList(),
                new PagedModel.PageMetadata(0, 0, 0));

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(parametreServiceClient.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);

        Parametre result = parametreClientHelper.findByCodi(CODI_PARAM);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Amb @EnableCaching actiu, findByCodi cacheja per codi i evita crides repetides")
    void springCaching_findByCodi_cachejaPerCodi() {
        try (AnnotationConfigApplicationContext context = createCachingContext()) {
            ParametreClientHelper helper = context.getBean(ParametreClientHelper.class);
            ParametreServiceClient client = context.getBean(ParametreServiceClient.class);
            HttpAuthorizationHeaderHelper authHelper = context.getBean(HttpAuthorizationHeaderHelper.class);

            assertThat(AopUtils.isAopProxy(helper)).isTrue();
            when(authHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
            when(client.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenReturn(pagedOf(Parametre.builder().codi(CODI_PARAM).valor("v1").build()));

            assertThat(helper.findByCodi(CODI_PARAM).getValor()).isEqualTo("v1");
            assertThat(helper.findByCodi(CODI_PARAM).getValor()).isEqualTo("v1");

            verify(client, times(1)).find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER));
            verify(authHelper, times(1)).getAuthorizationHeader();
        }
    }

    @Test
    @DisplayName("Després d'invalidar la caché d'un codi, torna a consultar el client")
    void springCaching_despresDeEvict_tornaAConsultarClient() {
        try (AnnotationConfigApplicationContext context = createCachingContext()) {
            ParametreClientHelper helper = context.getBean(ParametreClientHelper.class);
            ParametreServiceClient client = context.getBean(ParametreServiceClient.class);
            HttpAuthorizationHeaderHelper authHelper = context.getBean(HttpAuthorizationHeaderHelper.class);
            Cache cache = context.getBean(CacheManager.class).getCache(HazelCastCacheConfig.PARAMETRE_CACHE);

            when(authHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
            when(client.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenReturn(pagedOf(Parametre.builder().codi(CODI_PARAM).valor("v1").build()))
                    .thenReturn(pagedOf(Parametre.builder().codi(CODI_PARAM).valor("v2").build()));

            assertThat(helper.findByCodi(CODI_PARAM).getValor()).isEqualTo("v1");
            assertThat(cache).isNotNull();
            cache.evict(CODI_PARAM);

            assertThat(helper.findByCodi(CODI_PARAM).getValor()).isEqualTo("v2");
            verify(client, times(2)).find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER));
        }
    }

    @Test
    @DisplayName("Quan el paràmetre no existeix, el resultat null també queda cachejat")
    void springCaching_quanNoExisteix_cachejaResultatNull() {
        try (AnnotationConfigApplicationContext context = createCachingContext()) {
            ParametreClientHelper helper = context.getBean(ParametreClientHelper.class);
            ParametreServiceClient client = context.getBean(ParametreServiceClient.class);
            HttpAuthorizationHeaderHelper authHelper = context.getBean(HttpAuthorizationHeaderHelper.class);

            when(authHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
            when(client.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenReturn(PagedModel.of(Collections.emptyList(), new PagedModel.PageMetadata(0, 0, 0)));

            assertThat(helper.findByCodi(CODI_PARAM)).isNull();
            assertThat(helper.findByCodi(CODI_PARAM)).isNull();

            verify(client, times(1)).find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER));
            verify(authHelper, times(1)).getAuthorizationHeader();
        }
    }

    @Test
    @DisplayName("Quan el client llança FeignException, l'error no es cacheja i es reintenta")
    void springCaching_quanClientLlancaFeignException_noCachejaError() {
        try (AnnotationConfigApplicationContext context = createCachingContext()) {
            ParametreClientHelper helper = context.getBean(ParametreClientHelper.class);
            ParametreServiceClient client = context.getBean(ParametreServiceClient.class);
            HttpAuthorizationHeaderHelper authHelper = context.getBean(HttpAuthorizationHeaderHelper.class);

            when(authHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
            when(client.find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER)))
                    .thenThrow(mock(FeignException.class))
                    .thenReturn(pagedOf(Parametre.builder().codi(CODI_PARAM).valor("ok").build()));

            assertThatThrownBy(() -> helper.findByCodi(CODI_PARAM)).isInstanceOf(FeignException.class);
            assertThat(helper.findByCodi(CODI_PARAM).getValor()).isEqualTo("ok");
            assertThat(helper.findByCodi(CODI_PARAM).getValor()).isEqualTo("ok");

            verify(client, times(2)).find(any(), eq("codi:'" + CODI_PARAM + "'"), any(), any(), eq("UNPAGED"), any(), eq(AUTH_HEADER));
        }
    }

    private static AnnotationConfigApplicationContext createCachingContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getDefaultListableBeanFactory().setAllowCircularReferences(false);
        context.register(CachingTestConfig.class, ParametreClientHelper.class);
        context.refresh();
        return context;
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
