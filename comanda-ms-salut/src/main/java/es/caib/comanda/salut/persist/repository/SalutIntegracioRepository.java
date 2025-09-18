package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositori per a la gestió de les integracions relacionades amb les
 * informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutIntegracioRepository extends BaseRepository<SalutIntegracioEntity, Long> {

	List<SalutIntegracioEntity> findBySalut(SalutEntity salut);

    @Modifying
    @Query("DELETE FROM SalutIntegracioEntity s WHERE s.salut.id IN :salutIds")
    void deleteAllBySalutIdIn(@Param("salutIds") List<Long> salutIds);
}
