package es.caib.comanda.ms.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Esdeveniment SSE enviat als clients connectats.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ComandaSseEvent {

    public static final String SSE_EVENT_NAME = "comanda-event";

    private String type;
    private Serializable payload;
    private LocalDateTime timestamp;

}
