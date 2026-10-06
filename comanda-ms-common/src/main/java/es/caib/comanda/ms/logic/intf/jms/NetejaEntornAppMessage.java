package es.caib.comanda.ms.logic.intf.jms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Missatge JMS per a la neteja de les dades associades a un entorn d'aplicació.
 *
 * <p>Si {@code entornAppEsborrat} és fals (p. ex. l'acció manual de neteja d'estadístiques),
 * el mòdul d'estadístiques només esborra els fets. Si és cert, l'entorn d'aplicació s'ha
 * esborrat i s'esborren també el catàleg, els widgets i els dashboards associats; en aquest
 * cas {@code appId} i {@code entornId} identifiquen l'aplicació i l'entorn de l'entornApp
 * esborrat, ja que aquest ja no es pot consultar.</p>
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetejaEntornAppMessage {

    private Long entornAppId;
    private Long appId;
    private Long entornId;
    private boolean entornAppEsborrat;

    public NetejaEntornAppMessage(Long entornAppId) {
        this(entornAppId, null, null, false);
    }

}
