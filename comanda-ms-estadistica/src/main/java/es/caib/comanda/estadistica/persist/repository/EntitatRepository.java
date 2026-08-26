package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repositori per gestionar l'accés a dades de l'entitat EntitatEntity.
 *
 * Aquesta interfície proporciona operacions específiques per recuperar, consultar i gestionar informació
 * relacionada amb valors de dimensions dins del sistema. Hereta de BaseRepository per aprofitar les funcionalitats
 * bàsiques com CRUD, especificacions i altres metodologies configurables.
 *
 * @author Límit Tecnologies
 */
public interface EntitatRepository extends BaseRepository<EntitatEntity, Long> {

    Optional<EntitatEntity> findByCodi(String codi);

    Optional<EntitatEntity> findByCodiDir3(String codiDir3);

    Optional<EntitatEntity> findFirstByNom(String nom);

    Optional<EntitatEntity> findByCif(String cif);

    List<EntitatEntity> findByCodiIn(Collection<String> codis);
}
