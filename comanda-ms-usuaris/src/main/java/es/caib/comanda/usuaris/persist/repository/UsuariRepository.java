package es.caib.comanda.usuaris.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;

import java.util.Optional;

public interface UsuariRepository extends BaseRepository<UsuariEntity, Long> {

    Optional<UsuariEntity> findByCodi(String codi);

}
