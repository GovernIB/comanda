package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositori encarregat de gestionar operacions sobre l'entitat DashboardItemEntity en el context de persistència.
 *
 * Aquesta interfície defineix les operacions genèriques heretades de BaseRepository per manipular entitats DashboardItemEntity,
 * incloent-hi operacions CRUD, consultes personalitzades, i altres funcionalitats relacionades amb l'entitat associada.
 *
 * Autor: Límit Tecnologies
 */
public interface DashboardItemRepository extends BaseRepository<DashboardItemEntity, Long> {

    List<DashboardItemEntity> findByWidgetId(Long widgetId);

    @Query("SELECT MAX(d.posY + d.height) FROM DashboardItemEntity d WHERE d.dashboard.id = :dashboardId")
    Integer findMaxBottomPositionByDashboardId(@Param("dashboardId") Long dashboardId);

}
