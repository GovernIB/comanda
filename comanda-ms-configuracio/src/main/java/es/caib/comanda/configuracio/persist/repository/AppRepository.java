package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per a la gestió d'aplicacions.
 * 
 * @author Límit Tecnologies
 */
public interface AppRepository extends BaseRepository<AppEntity, Long> {

    List<AppEntity> findByActivaTrue();

}
