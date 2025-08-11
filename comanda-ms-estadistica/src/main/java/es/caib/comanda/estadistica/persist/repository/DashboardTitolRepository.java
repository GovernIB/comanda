package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DashboardTitolRepository extends BaseRepository<DashboardTitolEntity, Long> {

	@Query("SELECT MAX(t.posY + t.height) FROM DashboardTitolEntity t WHERE t.dashboard.id = :dashboardId")
	Integer findMaxBottomPositionByDashboardId(@Param("dashboardId") Long dashboardId);

}
