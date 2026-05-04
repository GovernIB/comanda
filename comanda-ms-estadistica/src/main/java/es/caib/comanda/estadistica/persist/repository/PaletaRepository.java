package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per accedir i gestionar les operacions de persistència relacionades amb l'entitat PaletaEntity.
 * Aquest repositori hereta funcionalitats del BaseRepository que proporciona operacions genèriques per als repositoris basats en JPA.
 *
 * @author Límit Tecnologies
 */
public interface PaletaRepository extends BaseRepository<PaletaEntity, Long> {
    List<PaletaEntity> findAllByOrderByNomAscIdAsc();

    Optional<PaletaEntity> findByNom(String nom);
}
