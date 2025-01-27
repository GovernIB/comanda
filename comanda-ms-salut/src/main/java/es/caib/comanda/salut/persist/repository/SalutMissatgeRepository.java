package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;

import java.util.List;

/**
 * Repositori per a la gestió dels missatges relacionats amb les
 * informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutMissatgeRepository extends BaseRepository<SalutMissatgeEntity, Long> {

	List<SalutMissatgeEntity> findBySalut(SalutEntity salut);

}
