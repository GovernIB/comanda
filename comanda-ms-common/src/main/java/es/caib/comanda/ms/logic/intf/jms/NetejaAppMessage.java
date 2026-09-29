package es.caib.comanda.ms.logic.intf.jms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Missatge JMS per a la neteja de dades estadístiques d'una aplicació (tots els entorns).
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetejaAppMessage {

    private Long appId;
    private List<Long> entornAppIds;
    private boolean esborrarCataleg;
    private boolean esborrarWidgets;

    public NetejaAppMessage(Long appId, List<Long> entornAppIds) {
        this(appId, entornAppIds, true, true);
    }

}
