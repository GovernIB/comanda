package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

public interface DashboardFiltreRepository extends BaseRepository<DashboardFiltreEntity, Long> {

    List<DashboardFiltreEntity> findByDashboardIdOrderByOrdre(Long dashboardId);

}
