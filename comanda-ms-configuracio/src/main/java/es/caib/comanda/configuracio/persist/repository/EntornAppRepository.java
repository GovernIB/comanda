package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.projection.EntornPermissionQueryProjection;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repositori per a la gestió d'aplicacions per entorn.
 * 
 * @author Límit Tecnologies
 */
public interface EntornAppRepository extends BaseRepository<EntornAppEntity, Long> {

	@EntityGraph(attributePaths = {"app.codi", "entorn.codi", "app.nom", "entorn.nom"}, type = EntityGraph.EntityGraphType.LOAD)
	List<EntornAppEntity> findByActivaTrueAndAppActivaTrue();

	Optional<EntornAppEntity> findByEntornIdAndAppId(Long entornId, Long appId);

	@Query("SELECT ae.id " +
		"FROM EntornAppEntity ae " +
		"WHERE ae.entorn.id = :entornId " +
		"AND ae.app.id = :appId ")
	Long getIdByEntornIdAndAppId(
		@Param("entornId") Long entornId,
		@Param("appId") Long appId);

	@Query("select ea.app.id" +
    " from EntornAppEntity ea" +
    " where ea.id in :entornAppIds ")
	Set<Long> findAppIdsByEntornAppIds(@Param("entornAppIds") Set<Long> entornAppIds);

	@Query("select " +
		"ea.id as entornAppId," +
		"ea.entorn.id as entornId," +
		"ea.app.id as appId" +
		" from EntornAppEntity ea")
	Set<EntornPermissionQueryProjection> findAllEntornPermissionQueryProjection();

}
