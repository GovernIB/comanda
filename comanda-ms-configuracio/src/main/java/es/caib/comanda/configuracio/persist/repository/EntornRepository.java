package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió d'entorns.
 *
 * @author Límit Tecnologies
 */
public interface EntornRepository extends BaseRepository<EntornEntity, Long> {

    List<EntornEntity> findAllByOrderByOrdreAsc();

    EntornEntity findByCodi(String codi);
}
