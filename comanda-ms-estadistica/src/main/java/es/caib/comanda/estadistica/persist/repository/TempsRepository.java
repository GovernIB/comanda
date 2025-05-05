package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.TempsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TempsRepository extends BaseRepository<TempsEntity, Long> {

    Optional<TempsEntity> findByData(LocalDate data);

}
