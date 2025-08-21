package es.caib.comanda.tasques.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.tasques.persist.entity.TascaEntity;

import java.util.Optional;

public interface TascaRepository extends BaseRepository<TascaEntity, Long> {

	Optional<TascaEntity> findByEntornAppIdAndIdentificador(Long entornAppId, String identificador);

}
