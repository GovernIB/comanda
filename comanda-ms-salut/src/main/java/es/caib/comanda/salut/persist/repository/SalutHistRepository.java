package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.persist.entity.SalutHistEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositori per a l'històric de canvis d'estat de salut.
 */
public interface SalutHistRepository extends BaseRepository<SalutHistEntity, Long> {

    SalutHistEntity findTopByEntornAppIdOrderByDataDescIdDesc(Long entornAppId);

    List<SalutHistEntity> findByEntornAppIdOrderByDataDescIdDesc(Long entornAppId);

    Optional<SalutHistEntity> findTopByEntornAppIdAndAppEstatInOrderByDataDesc(
            @Param("entornAppId") Long entornAppId,
            @Param("estatsEstables") List<SalutEstat> estatsEstables);

    @Query("SELECT MIN(sh.data) FROM SalutHistEntity sh " +
            "WHERE sh.entornAppId = :entornAppId " +
            "AND sh.data > :dataAnterior ")
    Optional<LocalDateTime> findSeguentData(
            @Param("entornAppId") Long entornAppId,
            @Param("dataAnterior")  LocalDateTime dataAnterior);

    @Modifying
    @Query("DELETE FROM SalutHistEntity s WHERE s.entornAppId = :entornAppId AND s.data < :dataLlindar")
    void deleteByEntornAppIdAndDataBefore(
            @Param("entornAppId") Long entornAppId,
            @Param("dataLlindar") LocalDateTime dataLlindar
    );
}
