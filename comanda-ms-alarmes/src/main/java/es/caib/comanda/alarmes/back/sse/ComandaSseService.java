package es.caib.comanda.alarmes.back.sse;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ComandaSseService {

    public SseEmitter subscribe();
    public void publish(ComandaSseEvent event);
}
