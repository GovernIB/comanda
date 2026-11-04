package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.AppGrupEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositori de base de dades pels grups d'aplicacions.
 */
@Repository
public interface AppGrupRepository extends BaseRepository<AppGrupEntity, Long> {

    Optional<AppGrupEntity> findByNom(String nom);
}
