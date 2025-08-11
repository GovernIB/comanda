package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per a la gestió d'aplicacions per entorn.
 * 
 * @author Límit Tecnologies
 */
public interface EntornAppRepository extends BaseRepository<EntornAppEntity, Long> {

	@EntityGraph(attributePaths = {"app.codi", "entorn.codi"}, type = EntityGraph.EntityGraphType.LOAD)
	List<EntornAppEntity> findByActivaTrueAndAppActivaTrue();

	Optional<EntornAppEntity> findByEntornIdAndAppId(Long entornId, Long appId);

	@Query("SELECT ae.id " +
		"FROM EntornAppEntity ae " +
		"WHERE ae.entorn.id = :entornId " +
		"AND ae.app.id = :appId ")
	Long getIdByEntornIdAndAppId(
		@Param("entornId") Long entornId,
		@Param("appId") Long appId);

}
