package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.persist.entity.SalutEntornAppEstatsEntity;

import java.util.Optional;

/**
 * Repositori per a la gestió de informació de salut sobre un entorn aplicació.
 * 
 * @author Límit Tecnologies
 */
public interface SalutEntornAppEstatsRepository extends BaseRepository<SalutEntornAppEstatsEntity, Long> {

    Optional<SalutEntornAppEstatsEntity> findByEntornAppId(Long entornAppId);

}
