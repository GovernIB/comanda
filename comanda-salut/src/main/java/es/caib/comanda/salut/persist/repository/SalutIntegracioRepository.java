package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;

import java.util.List;

/**
 * Repositori per a la gestió de les integracions relacionades amb les
 * informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutIntegracioRepository extends BaseRepository<SalutIntegracioEntity, Long> {

	List<SalutIntegracioEntity> findBySalut(SalutEntity salut);

}
