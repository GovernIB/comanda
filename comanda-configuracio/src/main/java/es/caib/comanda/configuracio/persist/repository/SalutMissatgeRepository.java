package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.SalutEntity;
import es.caib.comanda.configuracio.persist.entity.SalutMissatgeEntity;

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
