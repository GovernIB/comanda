package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

/**
 * Repositori per gestionar l'entitat IndicadorFormulaTermeEntity.
 *
 * @author Límit Tecnologies
 */
public interface IndicadorFormulaTermeRepository extends BaseRepository<IndicadorFormulaTermeEntity, Long> {

    List<IndicadorFormulaTermeEntity> findByIndicadorFormulaIdOrderByOrdreAsc(Long indicadorFormulaId);

}
