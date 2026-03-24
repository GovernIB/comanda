package es.caib.comanda.alarmes.back.controller;

import es.caib.comanda.alarmes.logic.service.sse.ComandaSseServiceImpl;
import es.caib.comanda.base.config.BaseConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseConfig.API_PATH + "/events")
@Tag(name = "24.1 Events", description = "Canal SSE d'events d'interfície")
public class EventsController {

    private final ComandaSseServiceImpl comandaSseService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return comandaSseService.subscribe();
    }

}
