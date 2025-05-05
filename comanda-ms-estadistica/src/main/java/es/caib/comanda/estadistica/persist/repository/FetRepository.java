package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.FetEntity;
import es.caib.comanda.estadistica.persist.entity.TempsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

public interface FetRepository extends BaseRepository<FetEntity, Long> {

    void deleteAllByTempsAndIndicator_EntornAppId(TempsEntity temps, Long entornAppId);

}
