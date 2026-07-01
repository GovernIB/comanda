package es.caib.comanda.ms.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Petició de publicació d'un esdeveniment SSE via Spring Application Event.
 * Qualsevol mòdul pot publicar aquesta petició; el mòdul d'alarmes l'escolta i l'envia als clients SSE.
 * Nota: quan els mòduls s'executin com a microserveis independents, reemplaçar per JMS/ActiveMQ.
 */
@Getter
@AllArgsConstructor
public class ComandaSsePublishRequest {

    private final ComandaSseEvent event;

}
