package es.caib.comanda.alarmes.persist.repository;

import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per a la gestió de les alarmes.
 *
 * @author Límit Tecnologies
 */
public interface AlarmaRepository extends BaseRepository<AlarmaEntity, Long> {

	Optional<AlarmaEntity> findTopByAlarmaConfigAndEstatOrderByIdDesc(
			AlarmaConfigEntity alarmaConfig,
			AlarmaEstat estat);

	List<AlarmaEntity> findByAlarmaConfigAndEstat(
			AlarmaConfigEntity alarmaConfig,
			AlarmaEstat estat);

	List<AlarmaEntity> findByAlarmaConfigCreatedByAndDataEnviamentIsNull(String username);
	List<AlarmaEntity> findByAlarmaConfigAdminAndDataEnviamentIsNull(boolean admin);

}
