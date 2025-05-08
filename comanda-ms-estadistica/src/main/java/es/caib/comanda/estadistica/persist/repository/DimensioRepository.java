package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per gestionar operacions de persistència relacionades amb l'entitat DimensioEntity.
 *
 * Aquesta interfície extén BaseRepository, proporcionant funcionalitats bàsiques de CRUD i mètodes específics per
 * gestionar les dimensions dins el sistema d'estadístiques. Principalment, permet cercar dimensions basades en el
 * seu codi i en l'entorn d'aplicació associat, així com obtenir totes les dimensions per a un entorn concret.
 *
 * @author Límit Tecnologies
 */
public interface DimensioRepository extends BaseRepository<DimensioEntity, Long> {

    Optional<DimensioEntity> findByCodiAndEntornAppId(String codi, Long entornAppId);

    List<DimensioEntity> findByEntornAppId(Long entornAppId);

}
