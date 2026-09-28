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
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publica per SSE el progrés de l'actualització de conselleries (acció FET_CONS de Dimensio), perquè el
 * frontend pugui mostrar una barra de progrés real mentre dura la crida. També evita que dues execucions
 * de FET_CONS per a la mateixa dimensió corrin alhora (doble clic, dues pestanyes, o dos usuaris): la
 * primera que arriba en queda com a propietària (tryStart) i les següents no fan cap feina, sinó que
 * reben a l'instant l'últim progrés conegut de l'execució ja en curs.
 * <p>
 * FetConsActionExecutor.exec() s'executa dins la transacció REQUIRED oberta per
 * BaseMutableResourceService.artifactActionExec(), que no fa commit fins que acaba tota la petició HTTP.
 * Com que ComandaSseEventListener escolta amb @TransactionalEventListener(phase = AFTER_COMMIT), si
 * publicàssim l'event dins d'aquesta transacció, tots els events de progrés s'enviarien de cop en acabar
 * (perdent l'efecte de progrés en temps real). Per això aquest mètode se suspèn de la transacció exterior amb
 * NOT_SUPPORTED (i no amb REQUIRES_NEW: aquesta obriria una segona transacció/connexió JDBC innecessària, ja
 * que aquí no es fa cap operació JPA — vegeu ConsultaEstadisticaHelper.getDadesWidget, on REQUIRES_NEW amb
 * consultes JPA reals va provocar un interbloqueig del pool de connexions en executar-se moltes peticions
 * concurrents). Sense cap transacció activa, ComandaSseEventListener (fallbackExecution=true) executa el
 * listener a l'instant en lloc d'esperar cap commit, aconseguint el mateix efecte de progrés en temps real.
 */
@Component
@RequiredArgsConstructor
public class DimensioFetConsProgressHelper {

    private final ApplicationEventPublisher eventPublisher;

    // Registre en memòria (procés únic, com la resta de l'estat de ComandaSseServiceImpl) de les execucions
    // de FET_CONS en curs per dimensió, amb l'últim progrés conegut de cadascuna.
    private final ConcurrentHashMap<Long, Progress> running = new ConcurrentHashMap<>();

    /**
     * Intenta registrar el cridador com a propietari de l'execució de FET_CONS per a aquesta dimensió.
     *
     * @return {@code true} si el cridador n'és el propietari i ha de fer la feina real; {@code false} si ja
     * n'hi havia una en curs (en aquest cas ja s'ha republicat l'últim progrés conegut per SSE i el cridador
     * no ha de fer res més).
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public boolean tryStart(Long dimensioId) {
        Progress initial = new Progress(dimensioId, 0, 0, false);
        Progress existing = running.putIfAbsent(dimensioId, initial);
        if (existing != null) {
            publish(existing);
            return false;
        }
        return true;
    }

    /**
     * Allibera el registre d'execució en curs per a aquesta dimensió. S'ha de cridar sempre (èxit o error)
     * quan el propietari (tryStart == true) acaba, perquè una propera crida hi pugui tornar a ser propietària.
     */
    public void finish(Long dimensioId) {
        running.remove(dimensioId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishProgress(Long dimensioId, int processats, int total) {
        Progress progress = new Progress(dimensioId, processats, total, false);
        running.replace(dimensioId, progress);
        publish(progress);
    }

    /**
     * Publica un darrer event de progrés amb error=true (amb l'últim total conegut), perquè qualsevol modal
     * oberta -sigui de qui hagi iniciat l'acció o de qui s'hi hagi enganxat via tryStart- sàpiga que el procés
     * real ha fallat i no es quedi esperant indefinidament un 100% que no arribarà mai.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void publishError(Long dimensioId) {
        Progress last = running.getOrDefault(dimensioId, new Progress(dimensioId, 0, 0, false));
        publish(new Progress(dimensioId, last.getProcessats(), last.getTotal(), true));
    }

    private void publish(Progress progress) {
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(
                ComandaSseEventTypes.DIMENSIO_FET_CONS_PROGRESS,
                progress,
                LocalDateTime.now(ZoneId.systemDefault()))));
    }

    @Getter
    @AllArgsConstructor
    public static class Progress implements Serializable {
        private final Long dimensioId;
        private final int processats;
        private final int total;
        private final boolean error;
    }

}
