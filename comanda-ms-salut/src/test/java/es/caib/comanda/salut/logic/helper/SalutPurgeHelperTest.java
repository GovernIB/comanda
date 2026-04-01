package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.salut.logic.helper.tx.SalutPurgeTxHelper;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalutPurgeHelperTest {

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

    @Mock
    private SalutPurgeTxHelper purgeTxHelper;

    private SalutPurgeHelper helper;

    @BeforeEach
    void setUp() {
        helper = new SalutPurgeHelper(
                salutRepository,
                salutIntegracioRepository,
                salutSubsistemaRepository,
                salutMissatgeRepository,
                salutDetallRepository,
                salutHistRepository,
                purgeTxHelper);
    }

    @Test
    void eliminarDadesSalutAntigues_quanNoHiHaIdsAntics_noExecutaCapBatch() {
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(5L, TipusRegistreSalut.HORA, LocalDateTime.of(2026, 3, 16, 9, 0)))
                .thenReturn(List.of());

        helper.eliminarDadesSalutAntigues(5L, TipusRegistreSalut.HORA, LocalDateTime.of(2026, 3, 16, 9, 0));

        verify(purgeTxHelper, never()).eliminarBatchEnNovaTransaccio(anyList());
    }

    @Test
    void eliminarDadesSalutAntigues_quanHiHaMesDunBatch_divideixElsIdsEnBlocsDe100() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 205).boxed().collect(Collectors.toList());
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(7L, TipusRegistreSalut.DIA, LocalDateTime.of(2026, 3, 16, 9, 30)))
                .thenReturn(ids);

        helper.eliminarDadesSalutAntigues(7L, TipusRegistreSalut.DIA, LocalDateTime.of(2026, 3, 16, 9, 30));

        verify(purgeTxHelper, times(3)).eliminarBatchEnNovaTransaccio(anyList());
        verify(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids.subList(0, 100));
        verify(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids.subList(100, 200));
        verify(purgeTxHelper).eliminarBatchEnNovaTransaccio(ids.subList(200, 205));
    }

    @Test
    void eliminarDadesSalutAntigues_quanHiHaLockTransitori_reintentaFinsQueElBatchFunciona() {
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(9L, TipusRegistreSalut.MINUTS, LocalDateTime.of(2026, 3, 16, 10, 0)))
                .thenReturn(List.of(1L, 2L));
        doThrow(new CannotAcquireLockException("locked"))
                .doNothing()
                .when(purgeTxHelper).eliminarBatchEnNovaTransaccio(List.of(1L, 2L));

        assertThatCode(() -> helper.eliminarDadesSalutAntigues(9L, TipusRegistreSalut.MINUTS, LocalDateTime.of(2026, 3, 16, 10, 0)))
                .doesNotThrowAnyException();

        verify(purgeTxHelper, times(2)).eliminarBatchEnNovaTransaccio(List.of(1L, 2L));
    }

    @Test
    void eliminarDadesSalutAntigues_quanElLockPersisteix_propagaLexcepcioDespresDelMaximDeReintents() {
        when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(11L, TipusRegistreSalut.MINUTS, LocalDateTime.of(2026, 3, 16, 10, 15)))
                .thenReturn(List.of(3L));
        doThrow(new CannotAcquireLockException("locked"))
                .when(purgeTxHelper).eliminarBatchEnNovaTransaccio(List.of(3L));

        assertThatCode(() -> helper.eliminarDadesSalutAntigues(11L, TipusRegistreSalut.MINUTS, LocalDateTime.of(2026, 3, 16, 10, 15)))
                .isInstanceOf(CannotAcquireLockException.class);

        verify(purgeTxHelper, times(3)).eliminarBatchEnNovaTransaccio(List.of(3L));
    }
}
