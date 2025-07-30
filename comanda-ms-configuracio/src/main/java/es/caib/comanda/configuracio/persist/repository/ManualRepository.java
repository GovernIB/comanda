package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppContextEntity;
import es.caib.comanda.configuracio.persist.entity.AppManualEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió de subsistemes.
 * 
 * @author Límit Tecnologies
 */
public interface ManualRepository extends BaseRepository<AppManualEntity, Long> {

	List<AppManualEntity> findByAppContext(AppContextEntity appContext);

}
