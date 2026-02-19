package es.caib.comanda.alarmes.persist.repository;

import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositori per a la gestió de les alarmes.
 *
 * @author Límit Tecnologies
 */
public interface AlarmaRepository extends BaseRepository<AlarmaEntity, Long> {

    Optional<AlarmaEntity> findTopByAlarmaConfigOrderByIdDesc(AlarmaConfigEntity alarmaConfig);
	Optional<AlarmaEntity> findTopByAlarmaConfigAndEstatOrderByIdDesc(
			AlarmaConfigEntity alarmaConfig,
			AlarmaEstat estat);

	List<AlarmaEntity> findByAlarmaConfigAndEstat(
			AlarmaConfigEntity alarmaConfig,
			AlarmaEstat estat);

	List<AlarmaEntity> findByAlarmaConfigCreatedByAndDataEnviamentIsNull(String username);
	List<AlarmaEntity> findByAlarmaConfigAdminAndDataEnviamentIsNull(boolean admin);

	@Query("SELECT " +
			"    DISTINCT a.alarmaConfig.createdBy " +
			"FROM " +
			"    AlarmaEntity a " +
			"WHERE " +
			"    a.dataActivacio >= ?1")
	List<String> findDistinctAlarmaConfigCreatedByDataActivacioAfter(LocalDateTime data);

	List<AlarmaEntity> findByAlarmaConfigAdminTrueAndEstat(AlarmaEstat estat);
	List<AlarmaEntity> findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(LocalDateTime data);

	List<AlarmaEntity> findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndEstat(
			String createdBy,
			AlarmaEstat estat);
	List<AlarmaEntity> findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(
			String createdBy,
			LocalDateTime data);

	@Modifying
	@Query("UPDATE " +
			"    AlarmaEntity a " +
			"SET " +
			"    a.estat = ?2 " +
			"WHERE " +
			"    a.alarmaConfig.admin = true " +
			"and a.estat = ?1")
	int updateAllEstatEsborradaAdmin(
			AlarmaEstat estat,
			AlarmaEstat nouEstat);

	@Modifying
	@Query("UPDATE " +
			"    AlarmaEntity a " +
			"SET " +
			"    a.estat = ?3 " +
			"WHERE " +
			"    a.alarmaConfig.admin = false " +
			"and a.alarmaConfig.createdBy = ?1 " +
			"and a.estat = ?2")
	int updateAllEstatEsborradaNoAdmin(
			String createdBy,
			AlarmaEstat estat,
			AlarmaEstat nouEstat);

}
