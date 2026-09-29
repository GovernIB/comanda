package es.caib.comanda.estadistica.logic.intf.service;

import java.util.List;

/**
 * Servei per a la neteja de dades estadístiques associades a una aplicació o aplicació-entorn.
 *
 * @author Límit Tecnologies
 */
public interface EstadisticaNetejaService {

    /**
     * Neteja exclusivament les dades operatives (fets) d'un entorn concret.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació
     */
    void netejaPerEntornApp(Long entornAppId);

    /**
     * Neteja estructural d'una aplicació: fets de tots els entorns, i opcionalment
     * catàleg (indicadors/dimensions) i widgets.
     *
     * @param appId Identificador de l'aplicació
     * @param entornAppIds Llista d'identificadors d'entorn d'aplicació associats
     * @param esborrarCataleg Si és cert, esborra indicadors i dimensions de tots els entorns
     * @param esborrarWidgets Si és cert, esborra també els widgets i items de dashboard
     */
    void netejaPerApp(Long appId, List<Long> entornAppIds, boolean esborrarCataleg, boolean esborrarWidgets);

    /**
     * Neteja estructural completa d'una aplicació (esborrant fets, catàleg i widgets).
     *
     * @param appId Identificador de l'aplicació
     * @param entornAppIds Llista d'identificadors d'entorn d'aplicació associats
     */
    default void netejaPerApp(Long appId, List<Long> entornAppIds) {
        netejaPerApp(appId, entornAppIds, true, true);
    }

}
