package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori per accedir i gestionar les operacions de persistència relacionades amb l'entitat PlantillaEntity.
 * Aquest repositori hereta funcionalitats del BaseRepository que proporciona operacions genèriques per als repositoris basats en JPA.
 *
 * @author Límit Tecnologies
 */
public interface PlantillaRepository extends BaseRepository<PlantillaEntity, Long> {

}