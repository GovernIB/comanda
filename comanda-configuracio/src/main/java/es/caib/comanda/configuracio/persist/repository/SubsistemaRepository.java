package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.SubsistemaEntity;

import java.util.List;

/**
 * Repositori per a la gestió de subsistemes.
 * 
 * @author Límit Tecnologies
 */
public interface SubsistemaRepository extends BaseRepository<SubsistemaEntity, Long> {

	List<SubsistemaEntity> findByApp(AppEntity app);

}
