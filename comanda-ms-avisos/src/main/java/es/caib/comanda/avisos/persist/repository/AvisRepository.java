package es.caib.comanda.avisos.persist.repository;

import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.Optional;

public interface AvisRepository extends BaseRepository<AvisEntity, Long> {

	Optional<AvisEntity> findByEntornAppIdAndIdentificador(Long entornAppId, String identificador);

}
