package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositori per gestionar l'accés a dades de l'entitat UnitatOrganitzativaEntity.
 *
 * Aquesta interfície proporciona operacions específiques per recuperar, consultar i gestionar informació
 * relacionada amb valors de dimensions dins del sistema. Hereta de BaseRepository per aprofitar les funcionalitats
 * bàsiques com CRUD, especificacions i altres metodologies configurables.
 *
 * @author Límit Tecnologies
 */
public interface UnitatOrganitzativaRepository extends BaseRepository<UnitatOrganitzativaEntity, Long> {

    boolean existsByCodi(String codi);

    Optional<UnitatOrganitzativaEntity> findByCodi(String codi);

    List<UnitatOrganitzativaEntity> findByCodiIn(Collection<String> codis);

    /** Totes les unitats d'un mateix arbre (mateixa arrel Dir3) - vegeu OrganitzativaTreeHelper. */
    List<UnitatOrganitzativaEntity> findByCodiUnitatArrel(String codiUnitatArrel);
}
