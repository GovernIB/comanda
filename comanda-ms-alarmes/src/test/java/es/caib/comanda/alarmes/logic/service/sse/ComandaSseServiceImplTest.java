package es.caib.comanda.alarmes.logic.service.sse;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @Test
    @DisplayName("subscribe registra la subscripcio amb el context de l'usuari")
    void subscribe_quanUsuariAutenticat_registraLaSubscripcioAmbLesDadesDelSubscriptor() {
        mockAuthenticatedUser("usuari1", true);

        SseEmitter emitter = comandaSseService.subscribe();
        Object subscription = getSubscriptions().get(0);

        assertThat(emitter).isNotNull();
        assertThat(getSubscriptions()).hasSize(1);
        assertThat(ReflectionTestUtils.getField(subscription, "userName")).isEqualTo("usuari1");
        assertThat(ReflectionTestUtils.getField(subscription, "admin")).isEqualTo(true);
    }

    @Test
    @DisplayName("resolveEventPayload carrega les alarmes actives per al subscritor")
    void resolveEventPayload_quanCanvienAlarmesActives_retornaPayloadPerSubscriptor() {
        mockAuthenticatedUser("admin1", true);
        comandaSseService.subscribe();
        Object subscription = getSubscriptions().get(0);
        List<Alarma.AlarmaReduidaResource> activeAlarms = List.of(
                new Alarma.AlarmaReduidaResource(1L),
                new Alarma.AlarmaReduidaResource(2L));
        when(alarmaService.findActiveAlarmIdsForSubscriber("admin1", true)).thenReturn(activeAlarms);
        ComandaSseEvent event = new ComandaSseEvent(
                ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED,
                null,
                LocalDateTime.now());

        ComandaSseEvent resolvedEvent = ReflectionTestUtils.invokeMethod(
                comandaSseService,
                "resolveEventPayload",
                subscription,
                event);

        assertThat(resolvedEvent).isNotNull();
        assertThat(resolvedEvent.getType()).isEqualTo(ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED);
        assertThat(resolvedEvent.getPayload()).isEqualTo(activeAlarms);
        verify(alarmaService).findActiveAlarmIdsForSubscriber("admin1", true);
    }

    @Test
    @DisplayName("resolveEventPayload manté els events no relacionats amb alarmes")
    void resolveEventPayload_quanEventGeneric_retornaElMateixEvent() {
        mockAuthenticatedUser("usuari1", false);
        comandaSseService.subscribe();
        Object subscription = getSubscriptions().get(0);
        ComandaSseEvent event = new ComandaSseEvent(
                ComandaSseEventTypes.CONNECTION_READY,
                "payload",
                LocalDateTime.now());

        ComandaSseEvent resolvedEvent = ReflectionTestUtils.invokeMethod(
                comandaSseService,
                "resolveEventPayload",
                subscription,
                event);

        assertThat(resolvedEvent).isSameAs(event);
        verifyNoInteractions(alarmaService);
    }

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
