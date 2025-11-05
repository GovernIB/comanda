package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori per a la gestió d'entorns.
 * 
 * @author Límit Tecnologies
 */
public interface EntornRepository extends BaseRepository<EntornEntity, Long> {

    EntornEntity findByCodi(String codi);
}
