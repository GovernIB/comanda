package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.DimensioValorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface DimensioValorRepository extends BaseRepository<DimensioValorEntity, Long> {

    Optional<DimensioValorEntity> findByDimensioAndValor(DimensioEntity dimensio, String valor);

    List<DimensioValorEntity> findByDimensio(DimensioEntity dimensio);

}
