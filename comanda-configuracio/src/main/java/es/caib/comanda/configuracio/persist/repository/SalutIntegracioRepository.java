package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.SalutEntity;
import es.caib.comanda.configuracio.persist.entity.SalutIntegracioEntity;

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
