package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AbstractActionProgressHelper")
class AbstractActionProgressHelperTest {

    private static final String TEST_EVENT_TYPE = "test.action.progress";

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TestActionProgressHelper helper;

    @Getter
    @AllArgsConstructor
    static class TestPayload implements Serializable {
        private final Long id;
        private final int processats;
        private final int total;
        private final boolean error;
    }

    static class TestActionProgressHelper extends AbstractActionProgressHelper<TestPayload> {
        public TestActionProgressHelper(ApplicationEventPublisher eventPublisher) {
            super(eventPublisher, TEST_EVENT_TYPE);
        }

        @Override
        protected TestPayload createProgress(Long id, int processats, int total, boolean error) {
            return new TestPayload(id, processats, total, error);
        }
    }

    @BeforeEach
    void setUp() {
        helper = new TestActionProgressHelper(eventPublisher);
    }

    @Test
    @DisplayName("publishProgress: annotat amb NOT_SUPPORTED per suspendre la transacció activa")
    void publishProgress_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = AbstractActionProgressHelper.class.getMethod(
            "publishProgress", Long.class, int.class, int.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("tryStart: annotat amb NOT_SUPPORTED per suspendre la transacció activa")
    void tryStart_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = AbstractActionProgressHelper.class.getMethod("tryStart", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("publishError: annotat amb NOT_SUPPORTED per suspendre la transacció activa")
    void publishError_noObreTransaccioPropia() throws NoSuchMethodException {
        Method method = AbstractActionProgressHelper.class.getMethod("publishError", Long.class);

        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.NOT_SUPPORTED);
    }

    @Test
    @DisplayName("tryStart: quan no hi ha cap execució en curs, retorna true i marca com a en curs")
    void tryStart_quanNoHiHaExecucioEnCurs_retornaTrue() {
        boolean started = helper.tryStart(10L);

        assertThat(started).isTrue();
        assertThat(helper.isRunning(10L)).isTrue();
    }

    @Test
    @DisplayName("tryStart: quan ja hi ha una execució en curs, retorna false i republica el darrer progrés")
    void tryStart_quanJaHiHaExecucioEnCurs_retornaFalseIRepublica() {
        helper.tryStart(10L);
        helper.publishProgress(10L, 5, 20);

        boolean secondStart = helper.tryStart(10L);

        assertThat(secondStart).isFalse();

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        List<ComandaSsePublishRequest> requests = captor.getAllValues();
        TestPayload republished = (TestPayload) requests.get(1).getEvent().getPayload();
        assertThat(republished.getId()).isEqualTo(10L);
        assertThat(republished.getProcessats()).isEqualTo(5);
        assertThat(republished.getTotal()).isEqualTo(20);
        assertThat(republished.isError()).isFalse();
    }

    @Test
    @DisplayName("tryStart: quan ja hi ha execució en curs però encara no s'ha cridat publishProgress, no publica cap event per evitar tancament prematur 0/0")
    void tryStart_quanEncaraNoHiHaProgres_noPublicaCapEvent() {
        helper.tryStart(10L);

        boolean secondStart = helper.tryStart(10L);

        assertThat(secondStart).isFalse();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("publishProgress: publica l'esdeveniment amb el tipus d'event correcte i les dades indicades")
    void publishProgress_publicaEvent() {
        helper.tryStart(10L);
        helper.publishProgress(10L, 3, 10);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());

        ComandaSseEvent event = captor.getValue().getEvent();
        assertThat(event.getType()).isEqualTo(TEST_EVENT_TYPE);

        TestPayload payload = (TestPayload) event.getPayload();
        assertThat(payload.getId()).isEqualTo(10L);
        assertThat(payload.getProcessats()).isEqualTo(3);
        assertThat(payload.getTotal()).isEqualTo(10);
        assertThat(payload.isError()).isFalse();
    }

    @Test
    @DisplayName("publishProgress: quan total és 0, publica correctament 0/0 per avisar els clients de finalització")
    void publishProgress_quanTotalEsZero_publica0de0() {
        helper.tryStart(10L);
        helper.publishProgress(10L, 0, 0);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TestPayload payload = (TestPayload) captor.getValue().getEvent().getPayload();
        assertThat(payload.getId()).isEqualTo(10L);
        assertThat(payload.getProcessats()).isZero();
        assertThat(payload.getTotal()).isZero();
        assertThat(payload.isError()).isFalse();
    }

    @Test
    @DisplayName("finish: allibera el registre de running")
    void finish_alliberaRegistre() {
        helper.tryStart(10L);
        assertThat(helper.isRunning(10L)).isTrue();

        helper.finish(10L);
        assertThat(helper.isRunning(10L)).isFalse();

        boolean canStartAgain = helper.tryStart(10L);
        assertThat(canStartAgain).isTrue();
    }

    @Test
    @DisplayName("publishError: publica un event amb error=true mantenint l'últim total")
    void publishError_publicaEventError() {
        helper.tryStart(10L);
        helper.publishProgress(10L, 7, 15);
        helper.publishError(10L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        TestPayload errorPayload = (TestPayload) captor.getAllValues().get(1).getEvent().getPayload();
        assertThat(errorPayload.getId()).isEqualTo(10L);
        assertThat(errorPayload.getProcessats()).isEqualTo(7);
        assertThat(errorPayload.getTotal()).isEqualTo(15);
        assertThat(errorPayload.isError()).isTrue();
    }

    @Test
    @DisplayName("publishError: quan no hi ha progrés previ, no llença cap excepció i publica error")
    void publishError_senseProgresPrevi() {
        helper.publishError(10L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());

        TestPayload errorPayload = (TestPayload) captor.getValue().getEvent().getPayload();
        assertThat(errorPayload.isError()).isTrue();
    }

    @Test
    @DisplayName("calculateProgressStep: retorna almenys 1 i no supera total / 20")
    void calculateProgressStep_calculaCorrectament() {
        assertThat(helper.calculateProgressStep(0)).isEqualTo(1);
        assertThat(helper.calculateProgressStep(5)).isEqualTo(1);
        assertThat(helper.calculateProgressStep(20)).isEqualTo(1);
        assertThat(helper.calculateProgressStep(40)).isEqualTo(2);
        assertThat(helper.calculateProgressStep(100)).isEqualTo(5);
        assertThat(helper.calculateProgressStep(1000)).isEqualTo(50);
    }

    @Test
    @DisplayName("Concurrència: múltiples fils intentant tryStart simultàniament, només un n'és el propietari")
    void tryStart_concurrentMultipleThreads_nomesUnEsPropietari() throws InterruptedException, ExecutionException {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            tasks.add(() -> {
                startLatch.await();
                return helper.tryStart(42L);
            });
        }

        // Llançar totes les tasques
        List<Future<Boolean>> futures = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            futures.add(executor.submit(task));
        }

        // Disparar tots els fils simultàniament
        startLatch.countDown();

        int trueCount = 0;
        int falseCount = 0;
        for (Future<Boolean> future : futures) {
            if (Boolean.TRUE.equals(future.get())) {
                trueCount++;
            } else {
                falseCount++;
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(trueCount).isEqualTo(1);
        assertThat(falseCount).isEqualTo(numThreads - 1);
    }
}
