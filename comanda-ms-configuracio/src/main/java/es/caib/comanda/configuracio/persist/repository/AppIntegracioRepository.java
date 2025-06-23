package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió d'integracions.
 * 
 * @author Límit Tecnologies
 */
public interface AppIntegracioRepository extends BaseRepository<AppIntegracioEntity, Long> {

	List<AppIntegracioEntity> findByEntornApp(EntornAppEntity entornApp);

}
