package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió d'integracions.
 * 
 * @author Límit Tecnologies
 */
public interface IntegracioRepository extends BaseRepository<AppIntegracioEntity, Long> {

	List<AppIntegracioEntity> findByApp(AppEntity app);
	List<AppIntegracioEntity> findByAppCodi(String codi);

}
