package es.caib.comanda.estadistica.logic.service;

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
import org.springframework.context.NoSuchMessageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a CacheServiceImpl")
class CacheServiceImplTest {

    @Mock
    private CacheHelper cacheHelper;

    @InjectMocks
    private CacheServiceImpl cacheService;

    // ========================================================================
    // 1. TESTOS PER A getOne
    // ========================================================================

    @Test
    @DisplayName("getOne: retorna cache especial quan l'id és 'TOTES'")
    void getOne_quanIdEsTotes_llavorsRetornaCacheEspecial() {
        // Act
        ComandaCache result = cacheService.getOne("TOTES", new String[0]);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("TOTES");
        verifyNoInteractions(cacheHelper);
    }

    @Test
    @DisplayName("getOne: retorna dades de la cache quan existeix")
    void getOne_quanCacheExisteix_llavorsRetornaDades() {
        // Arrange
        String cacheId = "testCache";
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció mocada");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            HazelcastCache mockCache = mock(HazelcastCache.class);
            IMap<Object, Object> nativeCache = mock(IMap.class);

            when(cacheHelper.getCache(cacheId)).thenReturn(mockCache);
            when(mockCache.getNativeCache()).thenReturn(nativeCache);
            when(nativeCache.size()).thenReturn(2);
            when(nativeCache.values()).thenReturn(java.util.List.of("value1", "value2"));

            // Act
            ComandaCache result = cacheService.getOne(cacheId, new String[0]);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(cacheId);
            assertThat(result.getEntrades()).isEqualTo(2);
            assertThat(result.getMida()).isGreaterThan(0);

            verify(cacheHelper).getCache(cacheId);
        }
    }

    @Test
    @DisplayName("getOne: retorna cache buida quan no existeix al CacheHelper")
    void getOne_quanCacheNoExisteix_llavorsRetornaCacheBuida() {
        // Arrange
        String cacheId = "nonexistentCache";
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció mocada");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            when(cacheHelper.getCache(cacheId)).thenReturn(null);

            // Act
            ComandaCache result = cacheService.getOne(cacheId, new String[0]);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("nonexistentCache");
            assertThat(result.getEntrades()).isEqualTo(0);
            assertThat(result.getMida()).isEqualTo(0L);
            verify(cacheHelper).getCache(cacheId);
        }
    }

    @Test
    @DisplayName("getOne: utilitza la clau i18n com a descripció quan llança NoSuchMessageException")
    void getOne_quanI18nLlancaExcepcio_llavorsUsaClauComDescripcio() {
        // Arrange
        String cacheId = "testCacheExcepcio";
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenThrow(new NoSuchMessageException("No message"));

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            when(cacheHelper.getCache(cacheId)).thenReturn(null);

            // Act
            ComandaCache result = cacheService.getOne(cacheId, new String[0]);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getDescripcio()).isEqualTo("es.caib.comanda.estadistica.cache." + cacheId);
        }
    }

    // ========================================================================
    // 2. TESTOS PER A findPage
    // ========================================================================

    @Test
    @DisplayName("findPage: retorna totes les caches disponibles sense filtre")
    void findPage_quanNoHiHaFiltre_llavorsRetornaTotesLesCaches() {
        // Arrange
        Set<String> cacheNames = new HashSet<>(Arrays.asList("cache1", "cache2"));
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció mocada");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            when(cacheHelper.getCacheNames()).thenReturn(cacheNames);
            when(cacheHelper.getCache(anyString())).thenReturn(null);

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<ComandaCache> result = cacheService.findPage("", "", new String[0], new String[0], pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(cacheHelper, times(1)).getCacheNames();
        }
    }

    @Test
    @DisplayName("findPage: filtra correctament quan el quickFilter coincideix amb l'id")
    void findPage_quanQuickFilterCoincideixAmbId_llavorsFiltraPerId() {
        // Arrange
        Set<String> cacheNames = new HashSet<>(Arrays.asList("cache1", "cache2"));
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció genèrica");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            when(cacheHelper.getCacheNames()).thenReturn(cacheNames);
            when(cacheHelper.getCache("cache1")).thenReturn(null);
            when(cacheHelper.getCache("cache2")).thenReturn(null);

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<ComandaCache> result = cacheService.findPage("2", "", new String[0], new String[0], pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("cache2");
        }
    }

    @Test
    @DisplayName("findPage: filtra correctament quan el quickFilter coincideix amb la descripció")
    void findPage_quanQuickFilterCoincideixAmbDescripcio_llavorsFiltraPerDescripcio() {
        // Arrange
        Set<String> cacheNames = new HashSet<>(Arrays.asList("cache1", "cache2"));
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        // Només "cache1" tindrà aquesta descripció
        when(mockI18nUtil.getI18nMessage("es.caib.comanda.estadistica.cache.cache1", null)).thenReturn("Descripció única de cache1");
        when(mockI18nUtil.getI18nMessage("es.caib.comanda.estadistica.cache.cache2", null)).thenReturn("Altra descripció");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            when(cacheHelper.getCacheNames()).thenReturn(cacheNames);
            when(cacheHelper.getCache("cache1")).thenReturn(null);
            when(cacheHelper.getCache("cache2")).thenReturn(null);

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<ComandaCache> result = cacheService.findPage("Descripció única de cache1", "", new String[0], new String[0], pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDescripcio()).isEqualTo("Descripció única de cache1");
        }
    }

    @Test
    @DisplayName("findPage: filtra correctament quan el quickFilter coincideix amb el nombre d'entrades")
    void findPage_quanQuickFilterCoincideixAmbEntrades_llavorsFiltraPerEntrades() {
        // Arrange
        Set<String> cacheNames = new HashSet<>(Arrays.asList("cache1", "cache2"));
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció genèrica");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            HazelcastCache mockCache1 = mock(HazelcastCache.class);
            IMap<Object, Object> nativeCache1 = mock(IMap.class);
            when(mockCache1.getNativeCache()).thenReturn(nativeCache1);
            when(nativeCache1.size()).thenReturn(5); // 5 entrades

            when(cacheHelper.getCacheNames()).thenReturn(cacheNames);
            when(cacheHelper.getCache("cache1")).thenReturn(mockCache1);
            when(cacheHelper.getCache("cache2")).thenReturn(null); // 0 entrades

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<ComandaCache> result = cacheService.findPage("5", "", new String[0], new String[0], pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("cache1");
            assertThat(result.getContent().get(0).getEntrades()).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("findPage: filtra correctament quan el quickFilter coincideix amb la mida")
    void findPage_quanQuickFilterCoincideixAmbMida_llavorsFiltraPerMida() {
        // Arrange
        Set<String> cacheNames = new HashSet<>(Arrays.asList("cache1", "cache2"));
        I18nUtil mockI18nUtil = mock(I18nUtil.class);
        when(mockI18nUtil.getI18nMessage(anyString(), any())).thenReturn("Descripció genèrica");

        try (MockedStatic<I18nUtil> mockedStatic = Mockito.mockStatic(I18nUtil.class)) {
            mockedStatic.when(I18nUtil::getInstance).thenReturn(mockI18nUtil);

            // cache1 tindrà una mida > 0
            HazelcastCache mockCache1 = mock(HazelcastCache.class);
            IMap<Object, Object> nativeCache1 = mock(IMap.class);
            when(mockCache1.getNativeCache()).thenReturn(nativeCache1);
            when(nativeCache1.size()).thenReturn(1);
            when(nativeCache1.values()).thenReturn(java.util.List.of("valor_llarg"));

            // cache2 serà null (mida 0)
            when(cacheHelper.getCacheNames()).thenReturn(cacheNames);
            when(cacheHelper.getCache("cache1")).thenReturn(mockCache1);
            when(cacheHelper.getCache("cache2")).thenReturn(null);

            Pageable pageable = PageRequest.of(0, 10);

            // Act
            Page<ComandaCache> result = cacheService.findPage("0", "", new String[0], new String[0], pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getMida()).isEqualTo(0L);
        }
    }

    // ========================================================================
    // 3. TESTOS PER A delete
    // ========================================================================

    @Test
    @DisplayName("delete: esborra totes les caches quan l'id és 'TOTES'")
    void delete_quanIdEsTotes_llavorsEsborraTotesLesCaches() throws Exception {
        // Act
        cacheService.delete("TOTES", null);

        // Assert
        verify(cacheHelper).evictAllCaches();
        verify(cacheHelper, never()).evictCache(anyString());
    }

    @Test
    @DisplayName("delete: esborra una cache específica quan l'id no és 'TOTES'")
    void delete_quanIdEsEspecific_llavorsEsborraAquellaCache() throws Exception {
        // Arrange
        String cacheId = "specificCache";

        // Act
        cacheService.delete(cacheId, null);

        // Assert
        verify(cacheHelper).evictCache(cacheId);
        verify(cacheHelper, never()).evictAllCaches();
    }
}
