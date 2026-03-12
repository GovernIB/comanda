package es.caib.comanda.alarmes.persist.repository;

import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió de les configuracions d'alarmes.
 *
 * @author Límit Tecnologies
 */
public interface AlarmaConfigRepository extends BaseRepository<AlarmaConfigEntity, Long> {
    List<AlarmaConfigEntity> findAllByEsborratFalse();
}
