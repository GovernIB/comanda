package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Mateix patró de tests que DimensioFetConsProgressHelperTest (acció FET_CONS de Dimensio), per a EntitatRefreshUOProgressHelper. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EntitatRefreshUOProgressHelper")
class EntitatRefreshUOProgressHelperTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EntitatRefreshUOProgressHelper entitatRefreshUOProgressHelper;

    @Test
    @DisplayName("publishProgress: no obre transacció pròpia (NOT_SUPPORTED)")
    void publishProgress_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = EntitatRefreshUOProgressHelper.class.getMethod(
            "publishProgress", Long.class, int.class, int.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("publishProgress: publica un ComandaSsePublishRequest amb el tipus d'event i les dades de progrés")
    void publishProgress_publicaEventAmbLesDadesDeProgres() {
        entitatRefreshUOProgressHelper.publishProgress(7L, 3, 10);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());

        ComandaSseEvent event = captor.getValue().getEvent();
        assertThat(event.getType()).isEqualTo(ComandaSseEventTypes.ENTITAT_REFRESH_UO_PROGRESS);
        EntitatRefreshUOProgressHelper.Progress progress = (EntitatRefreshUOProgressHelper.Progress) event.getPayload();
        assertThat(progress.getEntitatId()).isEqualTo(7L);
        assertThat(progress.getProcessats()).isEqualTo(3);
        assertThat(progress.getTotal()).isEqualTo(10);
        assertThat(progress.isError()).isFalse();
    }

    @Test
    @DisplayName("tryStart: no obre transacció pròpia (NOT_SUPPORTED)")
    void tryStart_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = EntitatRefreshUOProgressHelper.class.getMethod("tryStart", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("publishError: no obre transacció pròpia (NOT_SUPPORTED)")
    void publishError_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = EntitatRefreshUOProgressHelper.class.getMethod("publishError", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("tryStart: quan no hi ha cap execució en curs per a l'entitat, retorna true i se'n registra com a propietari")
    void tryStart_quanNoHiHaExecucioEnCurs_retornaTrue() {
        boolean owner = entitatRefreshUOProgressHelper.tryStart(7L);

        assertThat(owner).isTrue();
    }

    @Test
    @DisplayName("tryStart: quan ja hi ha una execució en curs per a la mateixa entitat, retorna false sense afectar-ne el registre")
    void tryStart_quanJaHiHaExecucioEnCurs_retornaFalse() {
        entitatRefreshUOProgressHelper.tryStart(7L);

        boolean owner = entitatRefreshUOProgressHelper.tryStart(7L);

        assertThat(owner).isFalse();
    }

    @Test
    @DisplayName("tryStart: quan ja hi ha una execució en curs, republica a l'instant l'últim progrés conegut")
    void tryStart_quanJaHiHaExecucioEnCurs_republicaElDarrerProgresConegut() {
        entitatRefreshUOProgressHelper.tryStart(7L);
        entitatRefreshUOProgressHelper.publishProgress(7L, 4, 10);

        entitatRefreshUOProgressHelper.tryStart(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        List<ComandaSsePublishRequest> requests = captor.getAllValues();
        EntitatRefreshUOProgressHelper.Progress republished =
            (EntitatRefreshUOProgressHelper.Progress) requests.get(1).getEvent().getPayload();
        assertThat(republished.getEntitatId()).isEqualTo(7L);
        assertThat(republished.getProcessats()).isEqualTo(4);
        assertThat(republished.getTotal()).isEqualTo(10);
        assertThat(republished.isError()).isFalse();
    }

    @Test
    @DisplayName("finish: allibera el registre perquè una crida posterior a tryStart torni a retornar true")
    void finish_alliberaElRegistre() {
        entitatRefreshUOProgressHelper.tryStart(7L);
        entitatRefreshUOProgressHelper.finish(7L);

        boolean owner = entitatRefreshUOProgressHelper.tryStart(7L);

        assertThat(owner).isTrue();
    }

    @Test
    @DisplayName("publishError: publica un darrer event de progrés amb error=true i l'últim total conegut")
    void publishError_publicaEventAmbErrorTrue() {
        entitatRefreshUOProgressHelper.tryStart(7L);
        entitatRefreshUOProgressHelper.publishProgress(7L, 4, 10);

        entitatRefreshUOProgressHelper.publishError(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        EntitatRefreshUOProgressHelper.Progress errorProgress =
            (EntitatRefreshUOProgressHelper.Progress) captor.getAllValues().get(1).getEvent().getPayload();
        assertThat(errorProgress.getEntitatId()).isEqualTo(7L);
        assertThat(errorProgress.getTotal()).isEqualTo(10);
        assertThat(errorProgress.isError()).isTrue();
    }

    @Test
    @DisplayName("publishError: quan no hi ha cap progrés registrat per a l'entitat, publica igualment l'error sense llançar excepció")
    void publishError_senseProgresRegistrat_noLlancaExcepcio() {
        entitatRefreshUOProgressHelper.publishError(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        EntitatRefreshUOProgressHelper.Progress errorProgress =
            (EntitatRefreshUOProgressHelper.Progress) captor.getValue().getEvent().getPayload();
        assertThat(errorProgress.isError()).isTrue();
    }

}
