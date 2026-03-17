package es.caib.comanda.estadistica.logic.service;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCache;
import es.caib.comanda.estadistica.logic.intf.model.cache.ComandaCache;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a CacheServiceImpl")
class CacheServiceImplTest {

    @Mock
    private CacheHelper cacheHelper;

    @InjectMocks
    private CacheServiceImpl cacheService;

    @Test
    @DisplayName("getOne retorna cache especial per id 'TOTES'")
    void getOne_quanIdTotes_retornaCacheEspecial() {
        // Act
        ComandaCache result = cacheService.getOne("TOTES", new String[0]);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("TOTES");
        verifyNoInteractions(cacheHelper);
    }

    @Test
    @DisplayName("getOne retorna cache amb dades quan existeix")
    void getOne_quanCacheExisteix_retornaDades() {
        // Arrange
        String cacheId = "testCache";
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció mocada");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            HazelcastCache mockCache = mock(HazelcastCache.class);
            HazelcastInstance instance = Hazelcast.newHazelcastInstance();
            IMap<Object, Object> nativeCache = instance.getMap(cacheId);
            nativeCache.put("key1", "value1");
            nativeCache.put("key2", "value2");


            when(cacheHelper.getCache(cacheId)).thenReturn(mockCache);
            when(mockCache.getNativeCache()).thenReturn(nativeCache);

            // Act
            ComandaCache result = cacheService.getOne(cacheId, new String[0]);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(cacheId);
            assertThat(result.getEntrades()).isEqualTo(2);
            assertThat(result.getMida()).isGreaterThan(0);
            verify(cacheHelper).getCache(cacheId);

            // Neteja Hazelcast
            instance.shutdown();
        }
    }

    @Test
    @DisplayName("getOne retorna cache buida quan no existeix")
    void getOne_quanCacheNoExisteix_retornaCacheBuida() {
        // Arrange
        String cacheId = "nonexistentCache";
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció mocada");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            HazelcastInstance instance = Hazelcast.newHazelcastInstance();
            when(cacheHelper.getCache(cacheId)).thenReturn(null);

            // Act
            ComandaCache result = cacheService.getOne(cacheId, new String[0]);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull(); // No s'ha establert l'ID perquè la cache és null
            assertThat(result.getEntrades()).isEqualTo(0);
            assertThat(result.getMida()).isEqualTo(0L);
            verify(cacheHelper).getCache(cacheId);

            // Neteja Hazelcast
            instance.shutdown();
        }
    }

    @Test
    @DisplayName("findPage retorna totes les caches disponibles")
    void findPage_retornaLlistaDeCaches() {
        // Arrange
        Set<String> cacheNames = new HashSet(Arrays.asList("cache1", "cache2"));
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció mocada");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            String cacheId = "testCache";
            HazelcastInstance instance = Hazelcast.newHazelcastInstance();
            IMap<Object, Object> nativeCache = instance.getMap(cacheId);
            nativeCache.put("key1", "value1");
            nativeCache.put("key2", "value2");
            when(cacheHelper.getCacheNames()).thenReturn(cacheNames);
            when(cacheHelper.getCache(anyString())).thenReturn(null);

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<ComandaCache> result = cacheService.findPage("", "", new String[0], new String[0], pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(cacheHelper, times(2)).getCacheNames();

            // Neteja Hazelcast
            instance.shutdown();
        }
    }

    @Test
    @DisplayName("delete esborra totes les caches quan id és 'TOTES'")
    void delete_quanIdTotes_esborraTotsLesCaches() throws Exception {
        // Act
        cacheService.delete("TOTES", null);

        // Assert
        verify(cacheHelper).evictAllCaches();
        verify(cacheHelper, never()).evictCache(anyString());
    }

    @Test
    @DisplayName("delete esborra una cache específica")
    void delete_quanIdEspecific_esborraAquellaCache() throws Exception {
        // Arrange
        String cacheId = "specificCache";

        // Act
        cacheService.delete(cacheId, null);

        // Assert
        verify(cacheHelper).evictCache(cacheId);
        verify(cacheHelper, never()).evictAllCaches();
    }
}
