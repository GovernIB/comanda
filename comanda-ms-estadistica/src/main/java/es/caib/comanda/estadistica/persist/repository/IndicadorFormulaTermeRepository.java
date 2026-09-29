package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositori per gestionar l'entitat IndicadorFormulaTermeEntity.
 *
 * @author Límit Tecnologies
 */
public interface IndicadorFormulaTermeRepository extends BaseRepository<IndicadorFormulaTermeEntity, Long> {

    List<IndicadorFormulaTermeEntity> findByIndicadorFormulaIdOrderByOrdreAsc(Long indicadorFormulaId);

    @Modifying
    @Query("DELETE FROM IndicadorFormulaTermeEntity ft WHERE ft.indicadorFormula IN (SELECT i FROM IndicadorEntity i WHERE i.entornAppId = :entornAppId) OR ft.indicadorComponent IN (SELECT i FROM IndicadorEntity i WHERE i.entornAppId = :entornAppId)")
    void deleteByEntornAppId(@Param("entornAppId") Long entornAppId);

}
