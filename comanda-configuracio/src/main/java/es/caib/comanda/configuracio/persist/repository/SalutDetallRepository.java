package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.SalutDetallEntity;
import es.caib.comanda.configuracio.persist.entity.SalutEntity;

import java.util.List;

/**
 * Repositori per a la gestió dels detalls relacionats amb les
 * informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutDetallRepository extends BaseRepository<SalutDetallEntity, Long> {

	List<SalutDetallEntity> findBySalut(SalutEntity salut);

}
