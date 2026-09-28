package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publica per SSE el progrés de la sincronització d'unitats organitzatives des de Dir3 (acció REFRESH_UO
 * d'Entitat), perquè el frontend pugui mostrar una barra de progrés real mentre dura la crida. Mateix patró que
 * {@link DimensioFetConsProgressHelper} (acció FET_CONS de Dimensio) - vegeu la seva documentació per als detalls
 * de per què cal suspendre la transacció (NOT_SUPPORTED) en publicar cada event, i per què es controla amb un
 * tryStart/finish qui és el propietari real de l'execució en curs.
 */
@Component
@RequiredArgsConstructor
public class EntitatRefreshUOProgressHelper {

    private final ApplicationEventPublisher eventPublisher;

    // Registre en memòria (procés únic) de les execucions de REFRESH_UO en curs per entitat, amb l'últim
    // progrés conegut de cadascuna.
    private final ConcurrentHashMap<Long, Progress> running = new ConcurrentHashMap<>();

    /**
     * Intenta registrar el cridador com a propietari de l'execució de REFRESH_UO per a aquesta entitat.
     *
     * @return {@code true} si el cridador n'és el propietari i ha de fer la feina real; {@code false} si ja
     * n'hi havia una en curs (en aquest cas ja s'ha republicat l'últim progrés conegut per SSE i el cridador
     * no ha de fer res més).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean tryStart(Long entitatId) {
        Progress initial = new Progress(entitatId, 0, 0, false);
        Progress existing = running.putIfAbsent(entitatId, initial);
        if (existing != null) {
            publish(existing);
            return false;
        }
        return true;
    }

    /**
     * Allibera el registre d'execució en curs per a aquesta entitat. S'ha de cridar sempre (èxit o error)
     * quan el propietari (tryStart == true) acaba, perquè una propera crida hi pugui tornar a ser propietària.
     */
    public void finish(Long entitatId) {
        running.remove(entitatId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishProgress(Long entitatId, int processats, int total) {
        Progress progress = new Progress(entitatId, processats, total, false);
        running.replace(entitatId, progress);
        publish(progress);
    }

    /**
     * Publica un darrer event de progrés amb error=true (amb l'últim total conegut), perquè qualsevol modal
     * oberta -sigui de qui hagi iniciat l'acció o de qui s'hi hagi enganxat via tryStart- sàpiga que el procés
     * real ha fallat i no es quedi esperant indefinidament un 100% que no arribarà mai.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishError(Long entitatId) {
        Progress last = running.getOrDefault(entitatId, new Progress(entitatId, 0, 0, false));
        publish(new Progress(entitatId, last.getProcessats(), last.getTotal(), true));
    }

    private void publish(Progress progress) {
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(
                ComandaSseEventTypes.ENTITAT_REFRESH_UO_PROGRESS,
                progress,
                LocalDateTime.now())));
    }

    @Getter
    @AllArgsConstructor
    public static class Progress implements Serializable {
        private final Long entitatId;
        private final int processats;
        private final int total;
        private final boolean error;
    }

}
