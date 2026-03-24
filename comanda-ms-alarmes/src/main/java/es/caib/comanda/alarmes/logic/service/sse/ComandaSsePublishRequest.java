package es.caib.comanda.alarmes.logic.service.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComandaSsePublishRequest {

    private final ComandaSseEvent event;

}
