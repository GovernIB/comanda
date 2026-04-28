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

    Optional<AlarmaEntity> findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(AlarmaConfigEntity alarmaConfig);
	Optional<AlarmaEntity> findTopByAlarmaConfigAndEstatOrderByIdDesc(
			AlarmaConfigEntity alarmaConfig,
			AlarmaEstat estat);

	List<AlarmaEntity> findByAlarmaConfigAndEstat(
			AlarmaConfigEntity alarmaConfig,
			AlarmaEstat estat);

	List<AlarmaEntity> findByAlarmaConfigCreatedByAndDataEnviamentIsNull(String username);
	List<AlarmaEntity> findByAlarmaConfigAdminAndDataEnviamentIsNull(boolean admin);
    List<AlarmaEntity> findByEstatAndAlarmaConfigAdminTrue(AlarmaEstat estat);
    List<AlarmaEntity> findByEstatAndAlarmaConfigAdminFalseAndAlarmaConfigCreatedBy(AlarmaEstat estat, String createdBy);

	@Query("SELECT " +
			"    DISTINCT a.alarmaConfig.createdBy " +
			"FROM " +
			"    AlarmaEntity a " +
			"WHERE " +
			"    a.dataActivacio >= ?1 " +
			"    AND a.dataEnviament IS NULL")
	List<String> findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(LocalDateTime data);

	List<AlarmaEntity> findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(LocalDateTime data);

	List<AlarmaEntity> findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(
			String createdBy,
			LocalDateTime data);

	List<AlarmaEntity> findByAlarmaConfigAdminTrueAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(LocalDateTime data);

	@Query("SELECT " +
			"    DISTINCT a.alarmaConfig.createdBy " +
			"FROM " +
			"    AlarmaEntity a " +
			"WHERE " +
			"    a.alarmaConfig.notificacioFinalitzada = true " +
			"    AND a.dataFinalitzacio >= ?1")
	List<String> findDistinctAlarmaConfigCreatedByNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(LocalDateTime data);

	List<AlarmaEntity> findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(
			String createdBy,
			LocalDateTime data);

	@Modifying
	@Query("DELETE FROM AlarmaEntity a WHERE a.alarmaConfig = ?1 AND a.estat = ?2")
	void deleteByAlarmaConfigAndEstat(AlarmaConfigEntity alarmaConfig, AlarmaEstat estat);

	@Modifying
	@Query("UPDATE AlarmaEntity a SET a.dataFinalitzacio = ?2 WHERE a.alarmaConfig = ?1 AND a.dataFinalitzacio IS NULL")
	void finalizeByAlarmaConfig(AlarmaConfigEntity alarmaConfig, LocalDateTime dataFinalitzacio);

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
