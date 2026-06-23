package es.caib.comanda.ms.configuracio.helper;

import es.caib.comanda.configuracio.logic.helper.AppInfoEntornAppHelper;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppHistEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppHistRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppInfoEntornAppHelperTest {

    @Mock private EntornAppRepository entornAppRepository;
    @Mock private EntornAppHistRepository entornAppHistRepository;

    @Captor private ArgumentCaptor<EntornAppHistEntity> histCaptor;

    private AppInfoEntornAppHelper helper;
    private EntornAppEntity entornAppEntity;

    @BeforeEach
    void setUp() {
        helper = new AppInfoEntornAppHelper(entornAppRepository, entornAppHistRepository);

        entornAppEntity = new EntornAppEntity();
        entornAppEntity.setId(1L);
        entornAppEntity.setVersio("1.0.0");
        entornAppEntity.setRevisio("rev-abc");
    }

    @Test
    void storeAppInfo_whenNoChanges_shouldNotCreateHistory() {
        // Given
        AppInfo appInfo = crearAppInfo("1.0.0", "rev-abc");

        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // When
        helper.storeAppInfo(appInfo, 1L);

        // Then
        verify(entornAppHistRepository, never()).save(any());
        assertEquals("1.0.0", entornAppEntity.getVersio());
    }

    @Test
    void storeAppInfo_whenVersioChanges_shouldCreateHistoryWithCanviVersioTrue() {
        // Given
        AppInfo appInfo = crearAppInfo("1.1.0", "rev-abc");

        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // When
        helper.storeAppInfo(appInfo, 1L);

        // Then
        verify(entornAppHistRepository).save(histCaptor.capture());
        EntornAppHistEntity saved = histCaptor.getValue();
        assertEquals("1.1.0", saved.getVersio());
        assertEquals("rev-abc", saved.getRevisio());
        assertTrue(saved.isCanviVersio());
        assertNotNull(saved.getData());
    }

    @Test
    void storeAppInfo_whenRevisioChangesOnly_shouldCreateHistoryWithCanviVersioFalse() {
        // Given
        AppInfo appInfo = crearAppInfo("1.0.0", "rev-xyz");

        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // When
        helper.storeAppInfo(appInfo, 1L);

        // Then
        verify(entornAppHistRepository).save(histCaptor.capture());
        EntornAppHistEntity saved = histCaptor.getValue();
        assertFalse(saved.isCanviVersio());
        assertEquals("rev-xyz", saved.getRevisio());
    }

    @Test
    void storeAppInfo_whenBothChange_shouldCreateHistoryWithCanviVersioTrue() {
        // Given
        AppInfo appInfo = crearAppInfo("2.0.0", "rev-new");

        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // When
        helper.storeAppInfo(appInfo, 1L);

        // Then
        verify(entornAppHistRepository).save(histCaptor.capture());
        EntornAppHistEntity saved = histCaptor.getValue();
        assertTrue(saved.isCanviVersio());
        assertEquals("2.0.0", saved.getVersio());
        assertEquals("rev-new", saved.getRevisio());
    }

    @Test
    void storeAppInfo_whenAppInfoIsNull_shouldNotFail() {
        // Given
        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // When + Then
        assertDoesNotThrow(() -> helper.storeAppInfo(null, 1L));
        verify(entornAppHistRepository, never()).save(any());
    }

    @Test
    void storeAppInfo_whenEntornAppNotFound_shouldThrowException() {
        // Given
        when(entornAppRepository.findById(999L)).thenReturn(Optional.empty());
        AppInfo appInfo = crearAppInfo("1.0.0", "nope");

        // When + Then
        assertThrows(ResourceNotFoundException.class, () ->
                helper.storeAppInfo(appInfo, 999L));
    }

    @Test
    void storeAppInfo_whenOldValuesAreNull_shouldHandleSafely() {
        // Given
        entornAppEntity.setVersio(null);
        entornAppEntity.setRevisio(null);

        AppInfo appInfo = crearAppInfo("1.0.0", "rev-1");

        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // When
        helper.storeAppInfo(appInfo, 1L);

        // Then
        verify(entornAppHistRepository).save(histCaptor.capture());
        EntornAppHistEntity saved = histCaptor.getValue();
        assertTrue(saved.isCanviVersio());
        assertEquals("1.0.0", saved.getVersio());
    }

    private AppInfo crearAppInfo(String versio, String revisio) {
        return AppInfo.builder()
                .codi("CODI")
                .nom("NOM")
                .versio(versio)
                .revisio(revisio)
                .data(OffsetDateTime.now())
                .build();
    }
}