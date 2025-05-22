package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interfície que defineix els serveis per gestionar recursos de tipus "Fet".
 *
 * Aquesta interfície permet obtenir estadístiques associades a les aplicacions i els seus entorns, així com migrar dades i aplicar
 * filtres específics. Els mètodes ofereixen funcionalitats per manejar informació estructurada en períodes específics i filtres
 * per dimensions.
 *
 * Relació amb la classe "Fet":
 * - Un "Fet" encapsula dades estadístiques amb informació temporal, dimensions i indicadors. Aquesta interfície permet manipular
 *   i explotar aquestes dades segons necessitats específiques del sistema.
 */
public interface FetService extends ReadonlyResourceService<Fet, Long> {

    /**
     * Obté les dades estadístiques d'ahir relacionades amb l'entorn especificat d'una aplicació.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació del qual es desitja migrar les dades.
     */
    void obtenirFets(Long entornAppId);
    void obtenirFets(Long entornAppId, int dies);

    /**
     * Obté les estadístiques d'un entornAppId en un període.
     */
    List<Fet> getEstadistiquesPeriode(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi);

    /**
     * Obté les estadístiques d'un entornAppId en un període i filtrat per dimensions.
     */
    List<Fet> getEstadistiquesPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre);

}
