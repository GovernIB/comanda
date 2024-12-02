package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per a la gestió d'aplicacions.
 * 
 * @author Límit Tecnologies
 */
public interface AppRepository extends BaseRepository<AppEntity, Long> {

	List<AppEntity> findByActivaTrue();

}
