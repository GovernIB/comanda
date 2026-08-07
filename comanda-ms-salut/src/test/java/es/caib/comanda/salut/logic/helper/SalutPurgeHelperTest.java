package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.salut.logic.helper.tx.SalutPurgeTxHelper;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.repository.*;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;

import javax.persistence.LockTimeoutException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a SalutPurgeHelper")
class SalutPurgeHelperTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private SalutHistRepository salutHistRepository;
    @Mock private SalutPurgeTxHelper purgeTxHelper;

    @InjectMocks
    private SalutPurgeHelper helper;

    // ========================================================================
    // TESTOS EXISTENTS (Mantinguts i polits)
    // ========================================================================

    @Test
    @DisplayName("eliminarDadesSalutAntigues: no executa cap batch quan no hi ha IDs antics")
    void eliminarDadesSalutAntigues_quanNoHiHaIdsAntics_llavorsNoExecutaCapBatch() {
        // Arrange
        LocalDateTime data = LocalDateTime.of(2026, 3, 16, 9, 0);
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(5L, TipusRegistreSalut.HORA, data))
            .thenReturn(List.of());

        // Act
        helper.eliminarDadesSalutAntigues(5L, TipusRegistreSalut.HORA, data);

        // Assert
        verify(purgeTxHelper, never()).eliminarBatchEnNovaTransaccio(anyList());
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: divideix els IDs en blocs de 100 quan n'hi ha més d'un batch")
    void eliminarDadesSalutAntigues_quanHiHaMesDunBatch_llavorsDivideixElsIdsEnBlocsDe100() {
        // Arrange
        LocalDateTime data = LocalDateTime.of(2026, 3, 16, 9, 30);
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 205).boxed().collect(Collectors.toList());
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(7L, TipusRegistreSalut.DIA, data))
            .thenReturn(ids);

        // Act
        helper.eliminarDadesSalutAntigues(7L, TipusRegistreSalut.DIA, data);

        // Assert
        verify(purgeTxHelper, times(3)).eliminarBatchEnNovaTransaccio(anyList());
        verify(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids.subList(0, 100));
        verify(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids.subList(100, 200));
        verify(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids.subList(200, 205));
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: reintentar fins que funciona quan hi ha un lock transitori")
    void eliminarDadesSalutAntigues_quanHiHaLockTransitori_llavorsReintentaFinsQueElBatchFunciona() {
        // Arrange
        LocalDateTime data = LocalDateTime.of(2026, 3, 16, 10, 0);
        List<Long> ids = List.of(1L, 2L);
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(9L, TipusRegistreSalut.MINUTS, data))
            .thenReturn(ids);

        doThrow(new CannotAcquireLockException("locked"))
            .doNothing()
            .when(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids);

        // Act & Assert
        assertThatCode(() -> helper.eliminarDadesSalutAntigues(9L, TipusRegistreSalut.MINUTS, data))
            .doesNotThrowAnyException();

        verify(purgeTxHelper, times(2)).eliminarBatchEnNovaTransaccio(ids);
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: propaga l'excepció després del màxim de reintents si el lock persisteix")
    void eliminarDadesSalutAntigues_quanElLockPersisteix_llavorsPropagaLexcepcioDespresDelMaximDeReintents() {
        // Arrange
        LocalDateTime data = LocalDateTime.of(2026, 3, 16, 10, 15);
        List<Long> ids = List.of(3L);
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(11L, TipusRegistreSalut.MINUTS, data))
            .thenReturn(ids);

        doThrow(new CannotAcquireLockException("locked"))
            .when(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids);

        // Act & Assert
        assertThatCode(() -> helper.eliminarDadesSalutAntigues(11L, TipusRegistreSalut.MINUTS, data))
            .isInstanceOf(CannotAcquireLockException.class);

        verify(purgeTxHelper, times(3)).eliminarBatchEnNovaTransaccio(ids);
    }

    // ========================================================================
    // NOUS TESTOS PER A COBERTURA > 90%
    // ========================================================================

    @Test
    @DisplayName("eliminarHistoricSalutAntic: crida correctament al helper de transacció")
    void eliminarHistoricSalutAntic_quanEsCrida_llavorsCridaPurgeTxHelper() {
        // Arrange
        Long entornAppId = 10L;
        LocalDateTime data = LocalDateTime.now().minusDays(30);

        // Act
        helper.eliminarHistoricSalutAntic(entornAppId, data);

        // Assert
        verify(purgeTxHelper, times(1)).eliminarHistoricEnNovaTransaccio(entornAppId, data);
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: no fa res quan el repositori retorna null")
    void eliminarDadesSalutAntigues_quanIdsAnticsSonNull_llavorsNoExecutaCapBatch() {
        // Arrange
        LocalDateTime data = LocalDateTime.now();
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(anyLong(), any(), any()))
            .thenReturn(null);

        // Act
        helper.eliminarDadesSalutAntigues(5L, TipusRegistreSalut.HORA, data);

        // Assert
        verify(purgeTxHelper, never()).eliminarBatchEnNovaTransaccio(anyList());
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: reintentar quan es produeix LockTimeoutException")
    void eliminarDadesSalutAntigues_quanHiHaLockTimeout_llavorsReintentaFinsQueFunciona() {
        // Arrange
        LocalDateTime data = LocalDateTime.now();
        List<Long> ids = List.of(10L);
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(anyLong(), any(), any()))
            .thenReturn(ids);

        // Simulem un LockTimeoutException (que està dins de isLockAcquisitionException)
        LockTimeoutException lockEx = new LockTimeoutException("timeout");
        doThrow(lockEx)
            .doNothing()
            .when(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids);

        // Act & Assert
        assertThatCode(() -> helper.eliminarDadesSalutAntigues(12L, TipusRegistreSalut.HORA, data))
            .doesNotThrowAnyException();

        verify(purgeTxHelper, times(2)).eliminarBatchEnNovaTransaccio(ids);
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: reintentar quan es produeix LockAcquisitionException de Hibernate")
    void eliminarDadesSalutAntigues_quanHiHaHibernateLockAcquisitionException_llavorsReintentaFinsQueFunciona() {
        // Arrange
        LocalDateTime data = LocalDateTime.now();
        List<Long> ids = List.of(11L);
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(anyLong(), any(), any()))
            .thenReturn(ids);

        // Simulem l'excepció específica de Hibernate que també es captura
        LockAcquisitionException hibernateLockEx = new LockAcquisitionException("hibernate lock", new SQLException());
        doThrow(hibernateLockEx)
            .doNothing()
            .when(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids);

        // Act & Assert
        assertThatCode(() -> helper.eliminarDadesSalutAntigues(13L, TipusRegistreSalut.HORA, data))
            .doesNotThrowAnyException();

        verify(purgeTxHelper, times(2)).eliminarBatchEnNovaTransaccio(ids);
    }

    @Test
    @DisplayName("eliminarDadesSalutAntigues: propaga immediatament sense reintentar quan l'excepció no és de lock")
    void eliminarDadesSalutAntigues_quanHiHaExcepcioNoLock_llavorsPropagaImmediatamentSenseReintentar() {
        // Arrange
        LocalDateTime data = LocalDateTime.now();
        List<Long> ids = List.of(12L);
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(anyLong(), any(), any()))
            .thenReturn(ids);

        RuntimeException otherEx = new RuntimeException("Algun altre error de base de dades");
        doThrow(otherEx).when(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids);

        // Act & Assert
        assertThatCode(() -> helper.eliminarDadesSalutAntigues(14L, TipusRegistreSalut.HORA, data))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Algun altre error de base de dades");

        // Només s'hauria d'haver cridat 1 vegada, sense reintents
        verify(purgeTxHelper, times(1)).eliminarBatchEnNovaTransaccio(ids);
    }
}
