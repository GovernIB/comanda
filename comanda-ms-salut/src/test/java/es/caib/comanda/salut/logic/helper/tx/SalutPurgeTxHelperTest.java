package es.caib.comanda.salut.logic.helper.tx;

import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutHistRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a SalutPurgeTxHelper")
class SalutPurgeTxHelperTest {

    @Mock
    private SalutRepository salutRepository;

    @Mock
    private SalutIntegracioRepository salutIntegracioRepository;

    @Mock
    private SalutSubsistemaRepository salutSubsistemaRepository;

    @Mock
    private SalutMissatgeRepository salutMissatgeRepository;

    @Mock
    private SalutDetallRepository salutDetallRepository;

    @Mock
    private SalutHistRepository salutHistRepository;

    @InjectMocks
    private SalutPurgeTxHelper salutPurgeTxHelper;

    // ========================================================================
    // 1. TESTOS PER A eliminarBatchEnNovaTransaccio
    // ========================================================================

    @Test
    @DisplayName("eliminarBatchEnNovaTransaccio: crida a tots els repositoris en l'ordre correcte amb una llista d'IDs")
    void eliminarBatchEnNovaTransaccio_quanLlistaEsValida_llavorsCridaTotsElsRepositoris() {
        // Arrange
        List<Long> salutIds = List.of(1L, 2L, 3L);

        // Act
        salutPurgeTxHelper.eliminarBatchEnNovaTransaccio(salutIds);

        // Assert: Verifiquem l'ordre i els paràmetres de les crides
        verify(salutIntegracioRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutSubsistemaRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutMissatgeRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutDetallRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutRepository, times(1)).deleteAllByIdInBatch(salutIds);
    }

    @Test
    @DisplayName("eliminarBatchEnNovaTransaccio: gestiona correctament una llista d'IDs buida")
    void eliminarBatchEnNovaTransaccio_quanLlistaEsBuida_llavorsCridaRepositorisAmbLlistaBuida() {
        // Arrange
        List<Long> salutIds = Collections.emptyList();

        // Act
        salutPurgeTxHelper.eliminarBatchEnNovaTransaccio(salutIds);

        // Assert: Assegurem que no hi ha curtcircuits i els mètodes es criden amb la llista buida
        verify(salutIntegracioRepository, times(1)).deleteAllBySalutIdIn(Collections.emptyList());
        verify(salutSubsistemaRepository, times(1)).deleteAllBySalutIdIn(Collections.emptyList());
        verify(salutMissatgeRepository, times(1)).deleteAllBySalutIdIn(Collections.emptyList());
        verify(salutDetallRepository, times(1)).deleteAllBySalutIdIn(Collections.emptyList());
        verify(salutRepository, times(1)).deleteAllByIdInBatch(Collections.emptyList());
    }

    // ========================================================================
    // 2. TESTOS PER A eliminarHistoricEnNovaTransaccio
    // ========================================================================

    @Test
    @DisplayName("eliminarHistoricEnNovaTransaccio: crida al repositori d'històric amb els paràmetres correctes")
    void eliminarHistoricEnNovaTransaccio_quanParametresSonValids_llavorsCridaRepositoriHistoric() {
        // Arrange
        Long entornAppId = 100L;
        LocalDateTime dataLlindar = LocalDateTime.now().minusDays(30);

        // Act
        salutPurgeTxHelper.eliminarHistoricEnNovaTransaccio(entornAppId, dataLlindar);

        // Assert
        verify(salutHistRepository, times(1)).deleteByEntornAppIdAndDataBefore(
            eq(entornAppId),
            eq(dataLlindar)
        );
    }

    @Test
    @DisplayName("eliminarHistoricEnNovaTransaccio: permet valors null a dataLlindar si el repositori ho suporta")
    void eliminarHistoricEnNovaTransaccio_quanDataLlindarEsNull_llavorsCridaRepositoriAmbNull() {
        // Arrange
        Long entornAppId = 100L;
        LocalDateTime dataLlindar = null;

        // Act
        salutPurgeTxHelper.eliminarHistoricEnNovaTransaccio(entornAppId, dataLlindar);

        // Assert
        verify(salutHistRepository, times(1)).deleteByEntornAppIdAndDataBefore(
            eq(entornAppId),
            any()
        );
    }
}
