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
     * Neteja completa de les dades estadístiques d'un entorn d'aplicació que s'ha esborrat:
     * <ul>
     *     <li>Fets de l'entornApp.</li>
     *     <li>Dashboards de l'aplicació i l'entorn (amb els seus elements, títols, filtres i preferits).</li>
     *     <li>Elements d'altres dashboards que mostren widgets de l'aplicació a l'entorn.</li>
     *     <li>Widgets que referencien el catàleg de l'entornApp o que s'han quedat sense cap element de dashboard.
     *     Els widgets que encara s'utilitzen en dashboards d'altres entorns no s'esborren: es reapunten al catàleg
     *     equivalent (mateix codi) d'un d'aquests entorns o, si no n'hi ha, es desvinculen.</li>
     *     <li>Catàleg (indicadors i dimensions) de l'entornApp.</li>
     * </ul>
     *
     * @param entornAppId Identificador de l'entorn d'aplicació esborrat
     * @param appId Identificador de l'aplicació de l'entornApp (pot ser null en missatges antics)
     * @param entornId Identificador de l'entorn de l'entornApp (pot ser null en missatges antics)
     */
    void netejaPerEntornAppEsborrat(Long entornAppId, Long appId, Long entornId);

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
