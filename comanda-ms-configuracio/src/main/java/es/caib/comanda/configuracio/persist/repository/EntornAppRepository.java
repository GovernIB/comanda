package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

/**
 * Repositori per a la gestió d'aplicacions per entorn.
 * 
 * @author Límit Tecnologies
 */
public interface EntornAppRepository extends BaseRepository<EntornAppEntity, Long> {

	@EntityGraph(attributePaths = {"app.codi", "entorn.codi"}, type = EntityGraph.EntityGraphType.LOAD)
	List<EntornAppEntity> findByActivaTrueAndAppActivaTrue();

}
