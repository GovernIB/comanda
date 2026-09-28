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

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DimensioFetConsProgressHelper")
class DimensioFetConsProgressHelperTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DimensioFetConsProgressHelper dimensioFetConsProgressHelper;

    @Test
    @DisplayName("publishProgress: no obre transacció pròpia (NOT_SUPPORTED), perquè aquest mètode no fa cap operació JPA i " +
        "REQUIRES_NEW obligaria a demanar una segona connexió JDBC del pool mentre la transacció de la petició HTTP en reté una altra")
    void publishProgress_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = DimensioFetConsProgressHelper.class.getMethod(
            "publishProgress", Long.class, int.class, int.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        // NOT_SUPPORTED suspèn la transacció REQUIRED oberta per artifactActionExec() sense crear-ne cap de nova:
        // com que ComandaSseEventListener té fallbackExecution=true, el listener AFTER_COMMIT s'executa igualment
        // de manera immediata i síncrona quan no hi ha cap transacció activa (vegeu
        // TransactionalApplicationListenerMethodAdapter.onApplicationEvent a spring-tx), obtenint el mateix efecte
        // de "progrés en temps real" que REQUIRES_NEW però sense poder demanar mai una connexió JDBC addicional.
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("publishProgress: publica un ComandaSsePublishRequest amb el tipus d'event i les dades de progrés")
    void publishProgress_publicaEventAmbLesDadesDeProgres() {
        dimensioFetConsProgressHelper.publishProgress(7L, 3, 10);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());

        ComandaSseEvent event = captor.getValue().getEvent();
        assertThat(event.getType()).isEqualTo(ComandaSseEventTypes.DIMENSIO_FET_CONS_PROGRESS);
        DimensioFetConsProgressHelper.Progress progress = (DimensioFetConsProgressHelper.Progress) event.getPayload();
        assertThat(progress.getDimensioId()).isEqualTo(7L);
        assertThat(progress.getProcessats()).isEqualTo(3);
        assertThat(progress.getTotal()).isEqualTo(10);
        assertThat(progress.isError()).isFalse();
    }

    @Test
    @DisplayName("tryStart: no obre transacció pròpia (NOT_SUPPORTED), perquè quan ja hi ha una execució en curs republica " +
        "l'últim progrés per SSE i necessita que no hi hagi transacció activa perquè el listener AFTER_COMMIT s'executi a l'instant")
    void tryStart_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = DimensioFetConsProgressHelper.class.getMethod("tryStart", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("publishError: no obre transacció pròpia (NOT_SUPPORTED), pel mateix motiu que publishProgress i tryStart")
    void publishError_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = DimensioFetConsProgressHelper.class.getMethod("publishError", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("tryStart: quan no hi ha cap execució en curs per a la dimensió, retorna true i se'n registra com a propietari")
    void tryStart_quanNoHiHaExecucioEnCurs_retornaTrue() {
        boolean owner = dimensioFetConsProgressHelper.tryStart(7L);

        assertThat(owner).isTrue();
    }

    @Test
    @DisplayName("tryStart: quan ja hi ha una execució en curs per a la mateixa dimensió, retorna false sense afectar-ne el registre")
    void tryStart_quanJaHiHaExecucioEnCurs_retornaFalse() {
        dimensioFetConsProgressHelper.tryStart(7L);

        boolean owner = dimensioFetConsProgressHelper.tryStart(7L);

        assertThat(owner).isFalse();
    }

    @Test
    @DisplayName("tryStart: quan ja hi ha una execució en curs, republica a l'instant l'últim progrés conegut " +
        "perquè una modal que s'acaba d'obrir el mostri de seguida, sense esperar el pròxim tick")
    void tryStart_quanJaHiHaExecucioEnCurs_republicaElDarrerProgresConegut() {
        dimensioFetConsProgressHelper.tryStart(7L);
        dimensioFetConsProgressHelper.publishProgress(7L, 4, 10);

        dimensioFetConsProgressHelper.tryStart(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        List<ComandaSsePublishRequest> requests = captor.getAllValues();
        DimensioFetConsProgressHelper.Progress republished =
            (DimensioFetConsProgressHelper.Progress) requests.get(1).getEvent().getPayload();
        assertThat(republished.getDimensioId()).isEqualTo(7L);
        assertThat(republished.getProcessats()).isEqualTo(4);
        assertThat(republished.getTotal()).isEqualTo(10);
        assertThat(republished.isError()).isFalse();
    }

    @Test
    @DisplayName("tryStart: quan encara no s'ha publicat cap progrés per a la dimensió en curs, republica 0/0 sense llançar excepció")
    void tryStart_quanEncaraNoHiHaProgresPublicat_republica0de0() {
        dimensioFetConsProgressHelper.tryStart(7L);

        dimensioFetConsProgressHelper.tryStart(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        DimensioFetConsProgressHelper.Progress republished =
            (DimensioFetConsProgressHelper.Progress) captor.getValue().getEvent().getPayload();
        assertThat(republished.getProcessats()).isZero();
        assertThat(republished.getTotal()).isZero();
    }

    @Test
    @DisplayName("finish: allibera el registre perquè una crida posterior a tryStart torni a retornar true")
    void finish_alliberaElRegistre() {
        dimensioFetConsProgressHelper.tryStart(7L);
        dimensioFetConsProgressHelper.finish(7L);

        boolean owner = dimensioFetConsProgressHelper.tryStart(7L);

        assertThat(owner).isTrue();
    }

    @Test
    @DisplayName("publishError: publica un darrer event de progrés amb error=true i l'últim total conegut, " +
        "perquè qualsevol modal oberta (propietària o piggyback) sàpiga que el procés real ha fallat")
    void publishError_publicaEventAmbErrorTrue() {
        dimensioFetConsProgressHelper.tryStart(7L);
        dimensioFetConsProgressHelper.publishProgress(7L, 4, 10);

        dimensioFetConsProgressHelper.publishError(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        DimensioFetConsProgressHelper.Progress errorProgress =
            (DimensioFetConsProgressHelper.Progress) captor.getAllValues().get(1).getEvent().getPayload();
        assertThat(errorProgress.getDimensioId()).isEqualTo(7L);
        assertThat(errorProgress.getTotal()).isEqualTo(10);
        assertThat(errorProgress.isError()).isTrue();
    }

    @Test
    @DisplayName("publishError: quan no hi ha cap progrés registrat per a la dimensió, publica igualment l'error sense llançar excepció")
    void publishError_senseProgresRegistrat_noLlancaExcepcio() {
        dimensioFetConsProgressHelper.publishError(7L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        DimensioFetConsProgressHelper.Progress errorProgress =
            (DimensioFetConsProgressHelper.Progress) captor.getValue().getEvent().getPayload();
        assertThat(errorProgress.isError()).isTrue();
    }

}
