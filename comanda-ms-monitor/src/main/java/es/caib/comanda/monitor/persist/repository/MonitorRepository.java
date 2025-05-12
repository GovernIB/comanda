package es.caib.comanda.monitor.persist.repository;

import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori per a la gestió de l'entitat MonitorEntity.
 *
 * Aquesta interfície defineix les operacions per a accedir, consultar, modificar i gestionar
 * dades relacionades amb l'entitat MonitorEntity a la base de dades.
 *
 * @author Límit Tecnologies
 */
public interface MonitorRepository extends BaseRepository<MonitorEntity, Long> {

}
