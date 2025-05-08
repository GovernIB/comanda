package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.IndicadorTaulaEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori per a la gestió d'operacions de persistència de l'entitat IndicadorTaulaEntity.
 *
 * Aquesta interfície proporciona funcionalitats bàsiques heretades de BaseRepository per dur a terme operacions CRUD
 * (crear, llegir, actualitzar i eliminar) així com consultes avançades mitjançant JpaSpecificationExecutor.
 *
 * @author Límit Tecnologies
 */
public interface IndicadorTaulaRepository extends BaseRepository<IndicadorTaulaEntity, Long> {

}