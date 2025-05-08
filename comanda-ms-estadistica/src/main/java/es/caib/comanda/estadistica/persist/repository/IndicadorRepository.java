package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.IndicadorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface IndicadorRepository extends BaseRepository<IndicadorEntity, Long> {

    Optional<IndicadorEntity> findByCodiAndEntornAppId(String codi, Long entornAppId);

    List<IndicadorEntity> findByEntornAppId(Long entornAppId);

}
