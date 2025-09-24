package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositori per a la gestió d'informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutRepository extends BaseRepository<SalutEntity, Long> {


	SalutEntity findTopByEntornAppIdOrderByIdDesc(Long entornAppId);

    SalutEntity findTopByEntornAppIdAndTipusRegistreOrderByIdDesc(Long entornAppId, TipusRegistreSalut tipusRegistre);
	List<SalutEntity> findByEntornAppIdAndTipusRegistreAndDataBefore(Long entornAppId, TipusRegistreSalut tipusRegistre, LocalDateTime data);
    @Query("SELECT s.id FROM SalutEntity s WHERE s.entornAppId = :entornAppId AND s.tipusRegistre = :tipusRegistre AND s.data < :data")
    List<Long> findIdsByEntornAppIdAndTipusRegistreAndDataBefore(
            @Param("entornAppId") Long entornAppId,
            @Param("tipusRegistre") TipusRegistreSalut tipusRegistre,
            @Param("data") LocalDateTime data
    );

	void deleteByDataBefore(LocalDateTime data);

    List<SalutEntity> findByEntornAppIdAndDataAfterAndTipusRegistreOrderById(Long entornAppId, LocalDateTime data, TipusRegistreSalut tipusRegistre);
    List<SalutEntity> findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(Long entornAppId, LocalDateTime data, TipusRegistreSalut tipusRegistre);

	@Query( " FROM SalutEntity s1 " +
			"WHERE s1.data in (SELECT MAX(s2.data) from SalutEntity s2 where s1.entornAppId = s2.entornAppId AND s2.data < :data) " +
			"  AND s1.entornAppId in (:entornAppIds) " +
            "  AND s1.tipusRegistre = es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut.MINUT " +
			"ORDER BY s1.entornAppId ASC")
	List<SalutEntity> informeSalutLast(
			@Param("entornAppIds") List<Long> entornAppIds,
			@Param("data") LocalDateTime data);

}
