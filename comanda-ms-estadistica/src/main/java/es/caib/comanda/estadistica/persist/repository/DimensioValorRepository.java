package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.DimensioValorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per gestionar l'accés a dades de l'entitat DimensioValorEntity.
 *
 * Aquesta interfície proporciona operacions específiques per recuperar, consultar i gestionar informació
 * relacionada amb valors de dimensions dins del sistema. Hereta de BaseRepository per aprofitar les funcionalitats
 * bàsiques com CRUD, especificacions i altres metodologies configurables.
 *
 * @author Límit Tecnologies
 */
public interface DimensioValorRepository extends BaseRepository<DimensioValorEntity, Long> {

    Optional<DimensioValorEntity> findByDimensioAndValor(DimensioEntity dimensio, String valor);

    List<DimensioValorEntity> findByDimensio(DimensioEntity dimensio);

}
