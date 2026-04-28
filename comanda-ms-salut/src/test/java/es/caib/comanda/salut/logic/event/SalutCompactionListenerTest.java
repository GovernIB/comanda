package es.caib.comanda.salut.logic.event;

import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SalutCompactionListenerTest {

    @Mock
    private SalutInfoHelper salutInfoHelper;

    @Test
    void onSalutInfoUpdated_quanLaCompactacioEsCorrecta_delegaAlHelper() {
        SalutCompactionListener listener = new SalutCompactionListener(salutInfoHelper);

        listener.onSalutInfoUpdated(new SalutInfoUpdatedEvent(1L, 2L));

        verify(salutInfoHelper).compactar(1L, 2L);
    }

    @Test
    void onSalutInfoUpdated_quanLaCompactacioFalla_noPropagaLexcepcio() {
        SalutCompactionListener listener = new SalutCompactionListener(salutInfoHelper);
        doThrow(new IllegalStateException("boom")).when(salutInfoHelper).compactar(1L, 2L);

        assertThatCode(() -> listener.onSalutInfoUpdated(new SalutInfoUpdatedEvent(1L, 2L)))
                .doesNotThrowAnyException();
    }

    @Test
    void onSalutCompactionFinished_quanElBuidatEsCorrecte_delegaAlHelper() {
        SalutCompactionListener listener = new SalutCompactionListener(salutInfoHelper);

        listener.onSalutCompactionFinished(new SalutCompactionFinishedEvent(3L, 4L));

        verify(salutInfoHelper).buidar(3L, 4L);
    }

    @Test
    void onSalutCompactionFinished_quanElBuidatFalla_noPropagaLexcepcio() {
        SalutCompactionListener listener = new SalutCompactionListener(salutInfoHelper);
        doThrow(new IllegalStateException("boom")).when(salutInfoHelper).buidar(3L, 4L);

        assertThatCode(() -> listener.onSalutCompactionFinished(new SalutCompactionFinishedEvent(3L, 4L)))
                .doesNotThrowAnyException();
    }
}
