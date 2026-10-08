package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper base genèric per gestionar el cicle de vida i la publicació per SSE del progrés d'accions llargues.
 * <p>
 * Encapsula:
 * <ul>
 *     <li>Concurrència: registre en memòria ({@code tryStart} / {@code finish}) per evitar execucions simultànies
 *         d'una mateixa acció per a un mateix identificador, republicant a l'instant el darrer progrés a noves crides.</li>
 *     <li>Publicació en temps real: transmissió d'events de progrés ({@code publishProgress}) i d'error ({@code publishError}).</li>
 *     <li>Gestió de transaccions: tots els mètodes de registre i publicació s'executen amb {@code NOT_SUPPORTED} per
 *         suspendre la transacció de la petició HTTP exterior, garantint que els listeners AFTER_COMMIT s'executin
 *         immediatament sense esperar el commit final ni demanar noves connexions JDBC al pool.</li>
 *     <li>Garanteix que {@code total == 0} (llistes buides) sempre publiqui el progrés inicial perquè els clients
 *         rebin l'avís de finalització immediata.</li>
 * </ul>
 *
 * @param <P> Tipus de dades del payload SSE que s'emetrà
 */
public abstract class AbstractActionProgressHelper<P extends Serializable> {

    private final ApplicationEventPublisher eventPublisher;
    private final String eventType;

    // Registre en memòria de les execucions en curs per identificador de recurs, amb l'últim progrés conegut.
    private final ConcurrentHashMap<Long, ProgressState> running = new ConcurrentHashMap<>();

    protected AbstractActionProgressHelper(ApplicationEventPublisher eventPublisher, String eventType) {
        this.eventPublisher = eventPublisher;
        this.eventType = eventType;
    }

    /**
     * Fabrica la instància concreta del payload SSE per a aquesta acció.
     *
     * @param id identificador del recurs
     * @param processats nombre d'elements processats
     * @param total nombre total d'elements
     * @param error si l'acció ha acabat amb error
     * @return instància del payload serialitzable
     */
    protected abstract P createProgress(Long id, int processats, int total, boolean error);

    /**
     * Retorna el tipus d'esdeveniment SSE configurat per a aquest helper.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Intenta registrar el cridador com a propietari de l'execució per a aquest identificador.
     *
     * @param id identificador del recurs
     * @return {@code true} si el cridador n'és el propietari i ha de fer la feina real; {@code false} si ja
     * n'hi havia una en curs. Si ja s'havia publicat algun progrés real, es republica a l'instant per SSE
     * perquè la nova modal el mostri de seguida. Si encara no s'ha publicat cap progrés (estat inicial en curs),
     * no es publica res per evitar enviar un fals 0/0 que el frontend interpretaria com a finalització prematura.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean tryStart(Long id) {
        ProgressState initial = new ProgressState(0, 0, false, false);
        ProgressState existing = running.putIfAbsent(id, initial);
        if (existing != null) {
            if (existing.isPublished()) {
                publish(id, existing);
            }
            return false;
        }
        return true;
    }

    /**
     * Allibera el registre d'execució en curs per a aquest recurs. S'ha de cridar sempre (èxit o error)
     * quan el propietari (tryStart == true) acaba, perquè una propera crida hi pugui tornar a ser propietària.
     *
     * @param id identificador del recurs
     */
    public void finish(Long id) {
        running.remove(id);
    }

    /**
     * Publica el progrés actual de l'acció per SSE i actualitza el registre intern.
     * Quan {@code total == 0}, s'ha de cridar amb {@code (id, 0, 0)} perquè el client rebi
     * la finalització immediata.
     *
     * @param id identificador del recurs
     * @param processats nombre d'elements processats
     * @param total nombre total d'elements
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishProgress(Long id, int processats, int total) {
        ProgressState progress = new ProgressState(processats, total, false, true);
        running.replace(id, progress);
        publish(id, progress);
    }

    /**
     * Publica un darrer event de progrés amb error=true (amb l'últim total conegut), perquè qualsevol modal
     * oberta sàpiga que el procés real ha fallat i no es quedi esperant indefinidament un 100% que no arribarà mai.
     *
     * @param id identificador del recurs
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishError(Long id) {
        ProgressState last = running.get(id);
        int processats = (last != null && last.isPublished()) ? last.getProcessats() : 0;
        int total = (last != null && last.isPublished()) ? last.getTotal() : 0;
        publish(id, new ProgressState(processats, total, true, true));
    }

    /**
     * Retorna si hi ha una execució en curs per a aquest identificador.
     */
    public boolean isRunning(Long id) {
        return running.containsKey(id);
    }

    /**
     * Com a màxim ~20 notificacions de progrés, independentment de la mida del total.
     * Mètode estàtic per evitar dependència d'stubs en tests unitaris dels serveis que mockegen el helper.
     */
    public static int calculateStep(int total) {
        return Math.max(1, total / 20);
    }

    /**
     * Com a màxim ~20 notificacions de progrés, independentment de la mida del total.
     */
    public int calculateProgressStep(int total) {
        return calculateStep(total);
    }

    private void publish(Long id, ProgressState state) {
        P payload = createProgress(id, state.getProcessats(), state.getTotal(), state.isError());
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(
                eventType,
                payload,
                LocalDateTime.now(ZoneId.systemDefault()))));
    }

    @Getter
    @AllArgsConstructor
    protected static class ProgressState implements Serializable {
        private final int processats;
        private final int total;
        private final boolean error;
        private final boolean published;
    }
}
