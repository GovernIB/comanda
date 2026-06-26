package es.caib.comanda.alarmes.persist.repository;

import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositori per a la gestió de les configuracions d'alarmes.
 *
 * @author Límit Tecnologies
 */
public interface AlarmaConfigRepository extends BaseRepository<AlarmaConfigEntity, Long> {
    List<AlarmaConfigEntity> findAllByEsborratFalse();
	List<AlarmaConfigEntity> findByEntornAppIdAndEsborratFalseAndAdminTrueOrderByOrdre(Long entornAppId);
	List<AlarmaConfigEntity> findByEntornAppIdAndEsborratFalseAndAdminFalseAndCreatedByOrderByOrdre(Long entornAppId, String createdBy);

    @Modifying
    @Query("DELETE FROM AlarmaConfigEntity a WHERE a.entornAppId = :entornAppId")
    void deleteByEntornAppId(@Param("entornAppId") Long entornAppId);
}
