package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Publica per SSE el progrés de la sincronització d'unitats organitzatives des de Dir3 (acció REFRESH_UO
 * d'Entitat), perquè el frontend pugui mostrar una barra de progrés real mentre dura la crida.
 * <p>
 * Vegeu {@link AbstractActionProgressHelper} per als detalls d'implementació i gestió de transaccions.
 */
@Component
public class EntitatRefreshUOProgressHelper extends AbstractActionProgressHelper<EntitatRefreshUOProgressHelper.Progress> {

    public EntitatRefreshUOProgressHelper(ApplicationEventPublisher eventPublisher) {
        super(eventPublisher, ComandaSseEventTypes.ENTITAT_REFRESH_UO_PROGRESS);
    }

    @Override
    protected Progress createProgress(Long entitatId, int processats, int total, boolean error) {
        return new Progress(entitatId, processats, total, error);
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
