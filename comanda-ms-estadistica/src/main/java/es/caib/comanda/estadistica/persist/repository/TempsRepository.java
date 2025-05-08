package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.TempsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.time.LocalDate;

/**
 * Interfície de repositori per a la gestió de l'entitat TempsEntity.
 *
 * Aquesta interfície proporciona operacions específiques per accedir i gestionar dades relacionades amb l'entitat de dimensió temporal,
 * a més de les funcionalitats estàndard heretades del BaseRepository.
 *
 * @author Límit Tecnologies
 */
public interface TempsRepository extends BaseRepository<TempsEntity, Long> {

    TempsEntity findByData(LocalDate data);

}
