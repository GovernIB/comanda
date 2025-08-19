package es.caib.comanda.monitor.persist.repository;

import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositori per a la gestió de l'entitat MonitorEntity.
 *
 * Aquesta interfície defineix les operacions per a accedir, consultar, modificar i gestionar
 * dades relacionades amb l'entitat MonitorEntity a la base de dades.
 *
 * @author Límit Tecnologies
 */
public interface MonitorRepository extends BaseRepository<MonitorEntity, Long> {

    @Query("SELECT m.id FROM MonitorEntity m WHERE m.data < :data ORDER BY m.id")
    List<Long> findIdsBeforeDate(@Param("data") LocalDateTime data, Pageable pageable);

}
