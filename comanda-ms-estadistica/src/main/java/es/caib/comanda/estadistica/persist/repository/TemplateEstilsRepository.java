package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.dashboard.TemplateEstilsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositori per a la gestió de plantilles d'estils.
 */
@Repository
public interface TemplateEstilsRepository extends BaseRepository<TemplateEstilsEntity, Long> {
    Optional<TemplateEstilsEntity> findByNom(String nom);
}
