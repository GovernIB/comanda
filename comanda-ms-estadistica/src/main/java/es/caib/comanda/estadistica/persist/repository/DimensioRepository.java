package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface DimensioRepository extends BaseRepository<DimensioEntity, Long> {

    Optional<DimensioEntity> findByCodiAndEntornAppId(String codi, Long entornAppId);

    List<DimensioEntity> findByEntornAppId(Long entornAppId);

}
