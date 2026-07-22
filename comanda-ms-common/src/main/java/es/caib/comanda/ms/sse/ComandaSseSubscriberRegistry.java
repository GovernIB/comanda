package es.caib.comanda.ms.sse;

/**
 * Proporciona informació sobre els subscriptors SSE actuals.
 * Implementada pel mòdul d'alarmes; injectada opcionalment pels mòduls que han de condicionar
 * la generació de payload SSE a l'existència de clients connectats.
 */
public interface ComandaSseSubscriberRegistry {

    boolean hasActiveSubscribers();

}
