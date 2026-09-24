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

import static org.assertj.core.api.Assertions.assertThat;
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
    }

}
