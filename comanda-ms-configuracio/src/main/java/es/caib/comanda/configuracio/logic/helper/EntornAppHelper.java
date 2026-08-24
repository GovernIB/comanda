package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.event.EntornAppEsborratEvent;
import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class EntornAppHelper {

    private final ApplicationEventPublisher eventPublisher;
    private final CacheHelper cacheHelper;

    /**
     * Neteja la cache de l'EntornApp i dispara l'eliminació de les seves entitats
     * relacionades en altres microserveis (amb reintents i fallback), notificant
     * posteriorment el canvi via SSE.
     *
     * @param entornAppId l'identificador de l'EntornApp eliminat.
     */
    public void logicAfterDelete(Long entornAppId) {
        cacheHelper.evictEntornAppCacheItem(entornAppId);
        eventPublisher.publishEvent(new EntornAppEsborratEvent(entornAppId));
        publishEntornAppChanged(entornAppId);
    }

    /**
     * Notifica als clients SSE connectats que la llista d'entorns-app ha canviat (alta, baixa,
     * modificació o activació/desactivació), ja sigui d'una app nova o d'una ja existent, perquè
     * puguin refer la llista sencera (a diferència de salut.changed, que només actualitza un
     * entorn-app ja mostrat).
     */
    public void publishEntornAppChanged(Long entornAppId) {
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(ComandaSseEventTypes.ENTORN_APP_CHANGED, entornAppId, LocalDateTime.now())));
    }

}
