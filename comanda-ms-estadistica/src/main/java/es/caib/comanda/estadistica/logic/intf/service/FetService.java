package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servei de consulta d'informació de estadística: Fets.
 *
 * @author Límit Tecnologies
 */
public interface FetService extends ReadonlyResourceService<Fet, Long> {

    /**
     * Obté informació estadística de totes les aplicacions actives.
     */
    void getEstadisticaInfo();

    /**
     * Obté les dades estadístiques d'ahir relacionades amb l'entorn especificat d'una aplicació.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació del qual es desitja migrar les dades.
     */
    void migrarDades(Long entornAppId);

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
