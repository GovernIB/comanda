package es.caib.comanda.ms.logic.intf.jms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Missatge JMS per a la neteja de dades associades a una aplicació-entorn esborrada.
 *
 * @author Límit Tecnologies
 */
@Getter
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class NetejaEntornAppMessage {

    private final Long entornAppId;

}
