package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutSubsistemaEntity;

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
