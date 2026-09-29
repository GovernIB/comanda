package es.caib.comanda.ms.logic.intf.jms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Missatge JMS per a la neteja de dades estadístiques (només fets) d'un entorn d'aplicació.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NetejaEntornAppMessage {

    private Long entornAppId;

}
