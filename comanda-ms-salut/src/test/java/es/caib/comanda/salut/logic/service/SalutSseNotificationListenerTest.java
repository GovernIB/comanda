package es.caib.comanda.salut.logic.service;

import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import es.caib.comanda.ms.sse.ComandaSseSubscriberRegistry;
import es.caib.comanda.salut.logic.event.SalutCompactionFinishedEvent;
import es.caib.comanda.salut.logic.helper.SalutEstatHelper;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaris per a SalutSseNotificationListener")
class SalutSseNotificationListenerTest {

    @Mock private SalutRepository salutRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private SalutEstatHelper salutEstatHelper;
    @Mock private ComandaSseSubscriberRegistry sseSubscriberRegistry;
    @Mock private SalutCompactionFinishedEvent mockEvent;

    @InjectMocks
    private SalutSseNotificationListener listener;

    private static final Long SALUT_ID = 10L;
    private static final Long ENTORN_APP_ID = 999L;
    private SalutEntity mockSalutEntity;

    @BeforeEach
    void setUp() {
        mockSalutEntity = new SalutEntity();
        mockSalutEntity.setId(SALUT_ID);
        mockSalutEntity.setEntornAppId(ENTORN_APP_ID);
        mockSalutEntity.setData(LocalDateTime.now());
        mockSalutEntity.setAppEstat(SalutEstat.UP);
        mockSalutEntity.setAppLatencia(150);
        mockSalutEntity.setBdEstat(SalutEstat.UP);
        mockSalutEntity.setBdLatencia(50);
        mockSalutEntity.setPeticioError(false);
        mockSalutEntity.setIntegracioUpCount(2);
        mockSalutEntity.setIntegracioWarnCount(0);
        mockSalutEntity.setIntegracioDownCount(0);
        mockSalutEntity.setIntegracioDesconegutCount(0);
        mockSalutEntity.setSubsistemaUpCount(5);
        mockSalutEntity.setSubsistemaWarnCount(1);
        mockSalutEntity.setSubsistemaDownCount(0);
        mockSalutEntity.setSubsistemaDesconegutCount(0);
        mockSalutEntity.setMissatgeErrorCount(0);
        mockSalutEntity.setMissatgeWarnCount(2);
        mockSalutEntity.setMissatgeInfoCount(10);
        lenient().when(mockEvent.getSalutId()).thenReturn(SALUT_ID);
        lenient().when(mockEvent.getEntornAppId()).thenReturn(ENTORN_APP_ID);
        ReflectionTestUtils.setField(listener, "sseSubscriberRegistry", sseSubscriberRegistry);
    }

    @AfterEach
    void tearDown() {
        ReflectionTestUtils.setField(listener, "sseSubscriberRegistry", sseSubscriberRegistry);
    }

    @Test
    @DisplayName("Ha de retornar immediatament si el salutId de l'event és nul")
    void onSalutCompactionFinished_SalutIdNul_RetornImmediat() {
        when(mockEvent.getSalutId()).thenReturn(null);

        listener.onSalutCompactionFinished(mockEvent);

        verifyNoInteractions(salutRepository, eventPublisher, salutEstatHelper, sseSubscriberRegistry);
    }

    @Test
    @DisplayName("Ha de retornar immediatament si no es troba l'entitat Salut a la BBDD")
    void onSalutCompactionFinished_EntitatNoTrobada_RetornImmediat() {
        when(salutRepository.findById(SALUT_ID)).thenReturn(Optional.empty());

        listener.onSalutCompactionFinished(mockEvent);

        verify(salutRepository).findById(SALUT_ID);
        verifyNoMoreInteractions(salutRepository);
        verifyNoInteractions(eventPublisher, salutEstatHelper, sseSubscriberRegistry);
    }

    @Test
    @DisplayName("Ha de publicar l'esdeveniment SSE correctament quan hi ha subscriptors actius")
    void onSalutCompactionFinished_ExitAmbSubscriptorsActius_PublicaEvent() {
        when(salutRepository.findById(SALUT_ID)).thenReturn(Optional.of(mockSalutEntity));
        when(sseSubscriberRegistry.hasActiveSubscribers()).thenReturn(true);

        Map<String, List<SalutInformeEstatItem>> mockEstatItems = Collections.singletonMap("GRUP1", Collections.emptyList());
        when(salutEstatHelper.computeEstatItemsPerAgrupacio(eq(ENTORN_APP_ID), eq(true))).thenReturn(mockEstatItems);
        listener.onSalutCompactionFinished(mockEvent);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ComandaSsePublishRequest publishedRequest = captor.getValue();
        assertNotNull(publishedRequest);
        assertEquals(ComandaSseEventTypes.SALUT_CHANGED, publishedRequest.getEvent().getType());
        SalutChangedSsePayload payload = (SalutChangedSsePayload) publishedRequest.getEvent().getPayload();
        assertEquals(ENTORN_APP_ID, payload.getEntornAppId());
        assertEquals(SalutEstat.UP, payload.getAppEstat());
        assertEquals(150, payload.getAppLatencia());
        assertEquals(5, payload.getSubsistemaUpCount());
        assertEquals(mockEstatItems, payload.getEstatItemsPerAgrupacio());
    }

    @Test
    @DisplayName("Ha de publicar l'esdeveniment SSE amb includeAll=false quan no hi ha subscriptors actius")
    void onSalutCompactionFinished_SenseSubscriptorsActius_CalculaAmbIncludeAllFalse() {
        when(salutRepository.findById(SALUT_ID)).thenReturn(Optional.of(mockSalutEntity));
        when(sseSubscriberRegistry.hasActiveSubscribers()).thenReturn(false);
        when(salutEstatHelper.computeEstatItemsPerAgrupacio(eq(ENTORN_APP_ID), eq(false))).thenReturn(null);

        listener.onSalutCompactionFinished(mockEvent);

        verify(salutEstatHelper).computeEstatItemsPerAgrupacio(ENTORN_APP_ID, false);
        verify(eventPublisher).publishEvent(any(ComandaSsePublishRequest.class));
    }

    @Test
    @DisplayName("Ha de publicar l'esdeveniment SSE amb includeAll=true quan el registre SSE és nul (entorn monolític)")
    void onSalutCompactionFinished_RegistreSseNul_CalculaAmbIncludeAllTrue() {
        ReflectionTestUtils.setField(listener, "sseSubscriberRegistry", null);
        when(salutRepository.findById(SALUT_ID)).thenReturn(Optional.of(mockSalutEntity));
        when(salutEstatHelper.computeEstatItemsPerAgrupacio(eq(ENTORN_APP_ID), eq(true))).thenReturn(null);

        listener.onSalutCompactionFinished(mockEvent);

        verify(salutEstatHelper).computeEstatItemsPerAgrupacio(ENTORN_APP_ID, true);
        verify(eventPublisher).publishEvent(any(ComandaSsePublishRequest.class));
    }

    @Test
    @DisplayName("Ha de continuar i publicar l'esdeveniment SSE encara que falli el càlcul d'estatItems")
    void onSalutCompactionFinished_ErrorCalculantEstatItems_PublicaEventAmbEstatItemsNul() {
        when(salutRepository.findById(SALUT_ID)).thenReturn(Optional.of(mockSalutEntity));
        when(sseSubscriberRegistry.hasActiveSubscribers()).thenReturn(true);
        doThrow(new RuntimeException("Error de càlcul")).when(salutEstatHelper).computeEstatItemsPerAgrupacio(anyLong(), anyBoolean());

        listener.onSalutCompactionFinished(mockEvent);
        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());

        SalutChangedSsePayload payload = (SalutChangedSsePayload) captor.getValue().getEvent().getPayload();
        assertNull(payload.getEstatItemsPerAgrupacio(), "Els estatItems han de ser nul·ls si el càlcul falla");
    }

    @Test
    @DisplayName("Ha de capturar i registrar l'excepció si falla la publicació de l'esdeveniment SSE")
    void onSalutCompactionFinished_ErrorPublicantEvent_CapturaExcepcio() {
        when(salutRepository.findById(SALUT_ID)).thenReturn(Optional.of(mockSalutEntity));
        when(sseSubscriberRegistry.hasActiveSubscribers()).thenReturn(true);
        when(salutEstatHelper.computeEstatItemsPerAgrupacio(anyLong(), anyBoolean())).thenReturn(Collections.emptyMap());

        doThrow(new RuntimeException("Error d'infraestructura SSE")).when(eventPublisher).publishEvent(any(ComandaSsePublishRequest.class));

        assertDoesNotThrow(() -> listener.onSalutCompactionFinished(mockEvent));

        verify(eventPublisher).publishEvent(any(ComandaSsePublishRequest.class));
    }
}
