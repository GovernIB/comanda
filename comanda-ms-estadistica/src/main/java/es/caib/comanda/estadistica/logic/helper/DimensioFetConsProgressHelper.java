package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Publica per SSE el progrés de l'actualització de conselleries (acció FET_CONS de Dimensio), perquè el
 * frontend pugui mostrar una barra de progrés real mentre dura la crida. També evita que dues execucions
 * de FET_CONS per a la mateixa dimensió corrin alhora (doble clic, dues pestanyes, o dos usuaris): la
 * primera que arriba en queda com a propietària (tryStart) i les següents no fan cap feina, sinó que
 * reben a l'instant l'últim progrés conegut de l'execució ja en curs.
 * <p>
 * Vegeu {@link AbstractActionProgressHelper} per als detalls d'implementació i gestió de transaccions.
 */
@Component
public class DimensioFetConsProgressHelper extends AbstractActionProgressHelper<DimensioFetConsProgressHelper.Progress> {

    public DimensioFetConsProgressHelper(ApplicationEventPublisher eventPublisher) {
        super(eventPublisher, ComandaSseEventTypes.DIMENSIO_FET_CONS_PROGRESS);
    }

    @Override
    protected Progress createProgress(Long dimensioId, int processats, int total, boolean error) {
        return new Progress(dimensioId, processats, total, error);
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
