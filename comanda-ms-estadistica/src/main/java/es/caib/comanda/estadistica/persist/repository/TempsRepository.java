package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.TempsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.time.LocalDate;

public interface TempsRepository extends BaseRepository<TempsEntity, Long> {

    TempsEntity findByData(LocalDate data);

}
