package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppContextEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió de subsistemes.
 * 
 * @author Límit Tecnologies
 */
public interface ContextRepository extends BaseRepository<AppContextEntity, Long> {

	List<AppContextEntity> findByEntornApp(EntornAppEntity entornApp);

}
