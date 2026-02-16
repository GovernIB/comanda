package es.caib.comanda.avisos.persist.repository;

import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.avisos.persist.entity.AvisLlegitEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AvisLlegitRepository extends BaseRepository<AvisLlegitEntity, Long> {

    boolean existsByUsuariAndAvis(String usuari, AvisEntity avis);

    @Query("SELECT max(avl.avis.id) FROM AvisLlegitEntity avl WHERE avl.usuari = :username AND avl.avis IN :avisEntities")
    Set<Long> findAvisIdsByUserIdAndAvisIn(@Param("username") String username, @Param("avisEntities") List<AvisEntity> avisEntities);

    void deleteByUsuariAndAvisIdIn(String usuari, List<Long> avisIds);

}
