package es.caib.comanda.estadistica.logic.dir3;

import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;

import java.util.List;


/**
 * Plugin per a obtenir l'arbre d'unitats organitzatives.
 *
 * @author Limit Tecnologies <limit@limit.es>
 */
public interface UnitatsOrganitzativesPlugin {

    /**
     * Retorna l'unitat organitzativa donat el seu codi.
     *
     * @param codi Codi de l'unitat organitzativa.
     * @return La unitat organitzativa.
     * @throws SistemaExternException Si es produeix un error al consultar les unitats organitzatives.
     */
    public UnitatOrganitzativaEntity findUnidad(String codi) throws SistemaExternException;

    public List<UnitatOrganitzativaEntity> findAll(String codi) throws SistemaExternException;

    /**
     * Retorna el codi de conselleria d'una unitat, pujant per l'arbre d'unitats fins a l'arrel per defecte
     * (paràmetre de configuració del govern, o el fallback intern si no està configurat).
     */
    public String getConselleria(String codi);

    /**
     * Retorna el codi de conselleria d'una unitat, pujant per l'arbre d'unitats fins a l'arrel indicada
     * (normalment el codiDir3 de l'Entitat a la qual pertany el fet, en lloc de l'arrel per defecte del govern).
     *
     * @param codi codi Dir3 de la unitat (òrgan gestor) de la qual es vol la conselleria
     * @param arrelCodi codi Dir3 de l'arrel a partir de la qual s'ha de considerar que ja no hi ha conselleria
     */
    public String getConselleria(String codi, String arrelCodi);

}
