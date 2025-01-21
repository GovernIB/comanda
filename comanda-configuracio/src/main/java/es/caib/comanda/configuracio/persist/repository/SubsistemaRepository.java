package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió de subsistemes.
 * 
 * @author Límit Tecnologies
 */
public interface SubsistemaRepository extends BaseRepository<AppSubsistemaEntity, Long> {

	List<AppSubsistemaEntity> findByApp(AppEntity app);
	List<AppSubsistemaEntity> findByAppCodi(String codi);

}
