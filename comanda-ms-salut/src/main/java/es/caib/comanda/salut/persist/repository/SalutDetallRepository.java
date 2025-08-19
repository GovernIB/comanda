package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.persist.entity.SalutDetallEntity;
import es.caib.comanda.salut.persist.entity.SalutEntity;

import java.util.List;

/**
 * Repositori per a la gestió dels detalls relacionats amb les
 * informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutDetallRepository extends BaseRepository<SalutDetallEntity, Long> {

	List<SalutDetallEntity> findBySalut(SalutEntity salut);

	void deleteBySalut(SalutEntity salut);
	void deleteAllBySalutIdIn(List<Long> salutIds);
}
