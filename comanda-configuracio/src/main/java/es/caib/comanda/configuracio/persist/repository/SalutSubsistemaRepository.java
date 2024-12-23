package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.persist.entity.SalutEntity;
import es.caib.comanda.configuracio.persist.entity.SalutSubsistemaEntity;

import java.util.List;

/**
 * Repositori per a la gestió dels subsistemes relacionats amb les
 * informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutSubsistemaRepository extends BaseRepository<SalutSubsistemaEntity, Long> {

	List<SalutSubsistemaEntity> findBySalut(SalutEntity salut);

}
