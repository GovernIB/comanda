package es.caib.comanda.alarmes.logic.service.sse;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a ComandaSseServiceImpl")
class ComandaSseServiceImplTest {

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private AlarmaService alarmaService;

    @InjectMocks
    private ComandaSseServiceImpl comandaSseService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========================================================================
    // 1. TESTOS PER A subscribe
    // ========================================================================

    @Test
    @DisplayName("subscribe: registra la subscripció amb el context de l'usuari")
    void subscribe_quanUsuariAutenticat_registraLaSubscripcioAmbLesDadesDelSubscriptor() {
        // Arrange
        mockAuthenticatedUser("usuari1", true);

        // Act
        SseEmitter emitter = comandaSseService.subscribe();

        // Assert
        assertThat(emitter).isNotNull();
        assertThat(getSubscriptions()).hasSize(1);
        Object subscription = getSubscriptions().get(0);
        assertThat(ReflectionTestUtils.getField(subscription, "userName")).isEqualTo("usuari1");
        assertThat(ReflectionTestUtils.getField(subscription, "admin")).isEqualTo(true);
    }

    @Test
    @DisplayName("subscribe: configura els callbacks onCompletion, onTimeout i onError i envia CONNECTION_READY")
    void subscribe_configuraCorrectamentElsCallbacksIEnviaEventInicial() throws IOException {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            // Act
            comandaSseService.subscribe();

            // Assert
            SseEmitter emitter = mocked.constructed().get(0);
            verify(emitter).onCompletion(any(Runnable.class));
            verify(emitter).onTimeout(any(Runnable.class));
            verify(emitter).onError(any());
            verify(emitter).send(any(SseEmitter.SseEventBuilder.class)); // Event CONNECTION_READY
        }
    }

    @Test
    @DisplayName("subscribe callback onCompletion: elimina la subscripció de la llista")
    void callbackOnCompletion_eliminaLaSubscripcio() {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);
            ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
            verify(emitter).onCompletion(completionCaptor.capture());

            // Act
            completionCaptor.getValue().run();

            // Assert
            assertThat(getSubscriptions()).isEmpty();
        }
    }

    @Test
    @DisplayName("subscribe callback onTimeout: elimina la subscripció i crida emitter.complete")
    void callbackOnTimeout_eliminaLaSubscripcioIFaComplete() {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);
            ArgumentCaptor<Runnable> timeoutCaptor = ArgumentCaptor.forClass(Runnable.class);
            verify(emitter).onTimeout(timeoutCaptor.capture());

            // Act
            timeoutCaptor.getValue().run();

            // Assert
            assertThat(getSubscriptions()).isEmpty();
            verify(emitter).complete();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("subscribe callback onError: elimina la subscripció de la llista")
    void callbackOnError_eliminaLaSubscripcio() {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);
            ArgumentCaptor<Consumer<Throwable>> errorCaptor = ArgumentCaptor.forClass(Consumer.class);
            verify(emitter).onError(errorCaptor.capture());

            // Act
            errorCaptor.getValue().accept(new IOException("Test error"));

            // Assert
            assertThat(getSubscriptions()).isEmpty();
        }
    }

    // ========================================================================
    // 2. TESTOS PER A hasActiveSubscribers
    // ========================================================================

    @Test
    @DisplayName("hasActiveSubscribers: retorna false quan no hi ha subscripcions")
    void hasActiveSubscribers_quanNoHiHaSubscripcions_retornaFalse() {
        // Act & Assert
        assertThat(comandaSseService.hasActiveSubscribers()).isFalse();
    }

    @Test
    @DisplayName("hasActiveSubscribers: retorna true quan hi ha almenys una subscripció")
    void hasActiveSubscribers_quanHiHaSubscripcions_retornaTrue() {
        // Arrange
        mockAuthenticatedUser("usuari1", false);
        comandaSseService.subscribe();

        // Act & Assert
        assertThat(comandaSseService.hasActiveSubscribers()).isTrue();
    }

    // ========================================================================
    // 3. TESTOS PER A publish
    // ========================================================================

    @Test
    @DisplayName("publish: envia l'event a totes les subscripcions actives")
    void publish_quanHiHaSubscriptorsActius_enviaEventATots() throws IOException {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);
            ComandaSseEvent event = new ComandaSseEvent(
                ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED, null, LocalDateTime.now());
            when(alarmaService.findActiveAlarmIdsForSubscriber("usuari1", false))
                .thenReturn(List.of(new Alarma.AlarmaReduidaResource(1L, 100L)));

            // Act
            comandaSseService.publish(event);

            // Assert
            // 1 crida per CONNECTION_READY (al subscribe) + 1 crida per l'event publicat
            verify(emitter, org.mockito.Mockito.times(2)).send(any(SseEmitter.SseEventBuilder.class));
            assertThat(getSubscriptions()).hasSize(1); // La subscripció continua activa
        }
    }

    @Test
    @DisplayName("publish: elimina les subscripcions que fallen en l'enviament (IOException)")
    void publish_quanEnviamentFalla_eliminaSubscripcioInactiva() throws IOException {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class, (mock, context) -> {
            // Fem que el segon enviament (el del publish) falli
            // El primer enviament (CONNECTION_READY al subscribe) el deixem passar
        })) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);
            // Ara configurem que el proper send() llanci IOException
            doThrow(new IOException("Client desconnectat")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

            ComandaSseEvent event = new ComandaSseEvent(
                ComandaSseEventTypes.CONNECTION_READY, null, LocalDateTime.now());

            // Act
            comandaSseService.publish(event);

            // Assert
            assertThat(getSubscriptions()).isEmpty(); // La subscripció ha estat eliminada
            verify(emitter).completeWithError(any(IOException.class));
        }
    }

    @Test
    @DisplayName("publish: no fa res si no hi ha subscripcions")
    void publish_quanNoHiHaSubscripcions_noFaRes() {
        // Arrange
        ComandaSseEvent event = new ComandaSseEvent(
            ComandaSseEventTypes.CONNECTION_READY, null, LocalDateTime.now());

        // Act
        comandaSseService.publish(event);

        // Assert
        assertThat(getSubscriptions()).isEmpty();
        verifyNoInteractions(alarmaService);
    }

    // ========================================================================
    // 4. TESTOS PER A sendHeartbeat
    // ========================================================================

    @Test
    @DisplayName("sendHeartbeat: envia un comentari SSE a totes les subscripcions actives")
    void sendHeartbeat_quanHiHaSubscriptorsActius_enviaComentariATots() throws IOException {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);

            // Act
            comandaSseService.sendHeartbeat();

            // Assert
            // 1 crida per CONNECTION_READY (al subscribe) + 1 crida pel comentari d'heartbeat
            verify(emitter, org.mockito.Mockito.times(2)).send(any(SseEmitter.SseEventBuilder.class));
            assertThat(getSubscriptions()).hasSize(1); // La subscripció continua activa
            verifyNoInteractions(alarmaService); // Un heartbeat no ha de resoldre alarmes
        }
    }

    @Test
    @DisplayName("sendHeartbeat: elimina les subscripcions que fallen en l'enviament (IOException)")
    void sendHeartbeat_quanEnviamentFalla_eliminaSubscripcioInactiva() throws IOException {
        // Arrange
        mockAuthenticatedUser("usuari1", false);

        try (MockedConstruction<SseEmitter> mocked = mockConstruction(SseEmitter.class)) {
            comandaSseService.subscribe();
            assertThat(getSubscriptions()).hasSize(1);

            SseEmitter emitter = mocked.constructed().get(0);
            doThrow(new IOException("Client desconnectat")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

            // Act
            comandaSseService.sendHeartbeat();

            // Assert
            assertThat(getSubscriptions()).isEmpty();
            verify(emitter).completeWithError(any(IOException.class));
        }
    }

    @Test
    @DisplayName("sendHeartbeat: no fa res si no hi ha subscripcions")
    void sendHeartbeat_quanNoHiHaSubscripcions_noFaRes() {
        // Act
        comandaSseService.sendHeartbeat();

        // Assert
        assertThat(getSubscriptions()).isEmpty();
        verifyNoInteractions(alarmaService);
    }

    // ========================================================================
    // 5. TESTOS PER A resolveEventPayload
    // ========================================================================

    @Test
    @DisplayName("resolveEventPayload: carrega les alarmes actives per al subscriptor quan l'event és ACTIVE_ALARMS_CHANGED")
    void resolveEventPayload_quanCanvienAlarmesActives_retornaPayloadPerSubscriptor() {
        // Arrange
        mockAuthenticatedUser("admin1", true);
        comandaSseService.subscribe();
        Object subscription = getSubscriptions().get(0);

        List<Alarma.AlarmaReduidaResource> activeAlarms = List.of(
            new Alarma.AlarmaReduidaResource(1L, 101L),
            new Alarma.AlarmaReduidaResource(2L, 202L));
        when(alarmaService.findActiveAlarmIdsForSubscriber("admin1", true)).thenReturn(activeAlarms);

        ComandaSseEvent event = new ComandaSseEvent(
            ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED, null, LocalDateTime.now());

        // Act
        ComandaSseEvent resolvedEvent = ReflectionTestUtils.invokeMethod(
            comandaSseService, "resolveEventPayload", subscription, event);

        // Assert
        assertThat(resolvedEvent).isNotNull();
        assertThat(resolvedEvent.getType()).isEqualTo(ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED);
        assertThat(resolvedEvent.getPayload()).isEqualTo(activeAlarms);
        assertThat(activeAlarms).extracting(Alarma.AlarmaReduidaResource::getEntornAppId)
            .containsExactly(101L, 202L);
        verify(alarmaService).findActiveAlarmIdsForSubscriber("admin1", true);
    }

    @Test
    @DisplayName("resolveEventPayload: manté el payload original per a events no relacionats amb alarmes")
    void resolveEventPayload_quanEventGeneric_retornaElMateixEvent() {
        // Arrange
        mockAuthenticatedUser("usuari1", false);
        comandaSseService.subscribe();
        Object subscription = getSubscriptions().get(0);

        ComandaSseEvent event = new ComandaSseEvent(
            ComandaSseEventTypes.CONNECTION_READY, "payload-original", LocalDateTime.now());

        // Act
        ComandaSseEvent resolvedEvent = ReflectionTestUtils.invokeMethod(
            comandaSseService, "resolveEventPayload", subscription, event);

        // Assert
        assertThat(resolvedEvent).isSameAs(event);
        assertThat(resolvedEvent.getPayload()).isEqualTo("payload-original");
        verifyNoInteractions(alarmaService);
    }

    // ========================================================================
    // MÈTODES AUXILIARS
    // ========================================================================

    @SuppressWarnings("unchecked")
    private List<Object> getSubscriptions() {
        return (List<Object>) ReflectionTestUtils.getField(comandaSseService, "subscriptions");
    }

    private void mockAuthenticatedUser(String userName, boolean admin) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userName);
        when(authenticationHelper.isCurrentUserInRole(authentication, BaseConfig.ROLE_ADMIN)).thenReturn(admin);
        SecurityContextHolder.setContext(securityContext);
    }
}
