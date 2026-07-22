package es.caib.comanda.ms.logic.intf.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Esdeveniment que es publica quan s'esborra una aplicació-entorn.
 * Permet que els altres microserveis netegin les dades associades.
 *
 * @author Límit Tecnologies
 */
@Getter
@RequiredArgsConstructor
public class EntornAppEsborratEvent {
    private final Long entornAppId;
}
