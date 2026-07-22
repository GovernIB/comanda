package es.caib.comanda.alarmes.persist.repository;

import es.caib.comanda.alarmes.persist.entity.AlarmaUsuariEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositori per a la gestió dels destinataris d'una alarma.
 *
 * @author Límit Tecnologies
 */
public interface AlarmaUsuariRepository extends BaseRepository<AlarmaUsuariEntity, Long> {

    @Modifying
    @Query("DELETE FROM AlarmaUsuariEntity au WHERE au.alarma IN (SELECT a FROM AlarmaEntity a WHERE a.entornAppId = :entornAppId)")
    void deleteByAlarmaEntornAppId(@Param("entornAppId") Long entornAppId);

}
