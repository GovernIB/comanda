package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;

import java.util.List;

/**
 * Repositori per a la gestió d'integracions.
 * 
 * @author Límit Tecnologies
 */
public interface IntegracioRepository extends BaseRepository<IntegracioEntity, Long> {

	List<IntegracioEntity> findByApp(AppEntity app);

}
