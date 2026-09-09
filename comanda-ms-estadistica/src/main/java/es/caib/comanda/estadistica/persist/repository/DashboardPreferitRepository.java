package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardPreferitEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Interfície que defineix el repositori per a la gestió de l'entitat DashboardPreferitEntity.
 *
 * Autor: Límit Tecnologies
 */
public interface DashboardPreferitRepository extends BaseRepository<DashboardPreferitEntity, Long> {

    boolean existsByUsuariCodiAndDashboardId(String usuariCodi, Long dashboardId);

    void deleteByUsuariCodiAndDashboardId(String usuariCodi, Long dashboardId);

}
