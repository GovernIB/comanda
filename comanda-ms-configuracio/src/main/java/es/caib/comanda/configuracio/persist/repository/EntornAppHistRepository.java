package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppHistEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositori per a la gestió d'històric de canvis externs d'entorn d'aplicació.
 *
 * @author Límit Tecnologies
 */
public interface EntornAppHistRepository extends BaseRepository<EntornAppHistEntity, Long> {

    List<EntornAppHistEntity> findByEntornAppOrderByDataDesc(EntornAppEntity entornApp);

    @Modifying
    long deleteByDataBefore(LocalDateTime data);

}
