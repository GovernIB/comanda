package es.caib.comanda.avisos.persist.repository;

import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AvisRepository extends BaseRepository<AvisEntity, Long> {

	Optional<AvisEntity> findByEntornAppIdAndIdentificador(Long entornAppId, String identificador);

    @Modifying
    int deleteByLastModifiedDateBefore(LocalDateTime dataLimit);

    @Query("SELECT a FROM AvisEntity a " +
            "WHERE a.id IN :avisIds " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM AvisLlegitEntity al " +
            "    WHERE al.avis = a AND al.usuari = :usuari" +
            ")")
    List<AvisEntity> findAvisosNoLlegitsByUsuariAndIds(
            @Param("usuari") String usuari,
            @Param("avisIds") Collection<Long> avisIds
    );
}
