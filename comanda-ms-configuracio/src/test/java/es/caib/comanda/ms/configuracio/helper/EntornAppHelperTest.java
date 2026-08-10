package es.caib.comanda.ms.configuracio.helper;

import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.event.EntornAppEsborratEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EntornAppHelperTest {

    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CacheHelper cacheHelper;

    @InjectMocks
    private EntornAppHelper entornAppHelper;

    @Test
    void logicAfterDelete_netetjaCacheIPublicaEsdeveniments() {
        Long entornAppId = 1L;
        entornAppHelper.logicAfterDelete(entornAppId);

        verify(cacheHelper).evictEntornAppCacheItem(entornAppId);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        var publishedEvents = eventCaptor.getAllValues();
        assertTrue(publishedEvents.stream().anyMatch(e ->
            e instanceof EntornAppEsborratEvent && ((EntornAppEsborratEvent) e).getEntornAppId().equals(entornAppId)
        ));
        assertTrue(publishedEvents.stream().anyMatch(e ->
            e instanceof ComandaSsePublishRequest &&
                ((ComandaSsePublishRequest) e).getEvent().getType().equals(ComandaSseEventTypes.ENTORN_APP_CHANGED) &&
                ((ComandaSsePublishRequest) e).getEvent().getPayload().equals(entornAppId)
        ));
    }

    @Test
    void publishEntornAppChanged_publicaEsdevenimentSseCorrecte() {
        Long entornAppId = 2L;

        entornAppHelper.publishEntornAppChanged(entornAppId);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ComandaSsePublishRequest request = captor.getValue();
        assertEquals(ComandaSseEventTypes.ENTORN_APP_CHANGED, request.getEvent().getType());
        assertEquals(entornAppId, request.getEvent().getPayload());
    }
}
