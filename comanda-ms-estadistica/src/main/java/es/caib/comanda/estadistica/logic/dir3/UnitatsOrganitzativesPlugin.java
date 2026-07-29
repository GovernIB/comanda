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
    public UnitatOrganitzativaEntity findUnidad(
        String codi) throws SistemaExternException;

    public List<UnitatOrganitzativaEntity> findAll();

    public String getConselleria(String codi);

}
