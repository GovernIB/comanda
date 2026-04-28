package es.caib.comanda.alarmes.logic.service.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComandaSseEvent implements Serializable {

    public static final String SSE_EVENT_NAME = "comanda-event";

    private String type;
    private Serializable payload;
    private LocalDateTime timestamp;

}
