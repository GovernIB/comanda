package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardLoadedEvent;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardLoadindErrorEvent;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultaEstadisticaAsyncHelper {

    private final ConsultaEstadisticaHelper consultaEstadisticaHelper;
    private final DashboardItemRepository dashboardItemRepository;
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManagerFactory entityManagerFactory;


    // @Async("asyncTaskExecutor")
    //    public void generateAsyncData(Long dashboardId) {
    //        EntityManager entityManager = entityManagerFactory.createEntityManager();
    //        try {
    //            entityManager.getTransaction().begin();
    //            DashboardEntity dashboard = entityManager.find(DashboardEntity.class, dashboardId);
    //
    //            processDashboardItems(dashboard, dashboardId);
    //
    //            entityManager.getTransaction().commit();
    //        } catch (Exception e) {
    //            entityManager.getTransaction().rollback();
    //            throw e;
    //        } finally {
    //            entityManager.close();
    //        }
    //
    //    }

    @Async("asyncTaskExecutor")
    public void generateAsyncData(DashboardItemEntity dashboardItem) {
        DashboardEntity dashboard = dashboardItem.getDashboard();
        try {
            Long inici = System.currentTimeMillis();

            // TODO: Eliminar això:
            if (dashboardItem.getId() == 22L) {
                inici = null;
            }
            // Fi TODO

            InformeWidgetItem loadedItem = consultaEstadisticaHelper.getDadesWidget(dashboardItem);
            DashboardLoadedEvent dashboardLoadedEvent = DashboardLoadedEvent.builder()
                    .dashboardId(dashboard.getId())
                    .dashboardItemId(dashboardItem.getId())
                    .informeWidgetItem(loadedItem)
                    .tempsCarrega(System.currentTimeMillis() - inici)
                    .build();
            eventPublisher.publishEvent(dashboardLoadedEvent);
            log.info("Dashboard {}: Item {} loaded", dashboard.getId(), dashboardItem.getId());
        } catch (Exception e) {
            log.error("Error generant informe widget. Item {}: {}", dashboardItem.getId(), e.getMessage(), e);
            handleWidgetError(dashboard, dashboardItem, e);
        }
    }

    private void handleWidgetError(DashboardEntity dashboard, DashboardItemEntity dashboardItem, Exception e) {
        EstadisticaWidgetEntity widget = dashboardItem.getWidget();
        InformeWidgetItem loadingErrorItem = InformeWidgetItem.builder()
                .dashboardItemId(dashboardItem.getId())
                .titol(widget.getTitol())
                .tipus(consultaEstadisticaHelper.determineWidgetType(dashboardItem))
                .posX(dashboardItem.getPosX())
                .posY(dashboardItem.getPosY())
                .width(dashboardItem.getWidth())
                .height(dashboardItem.getHeight())
                .error(true)
                .errorMsg("Error processing item " + dashboardItem.getId() + ": " + e.getMessage())
                .errorTrace(ExceptionUtils.getStackTrace(e))
                .build();

        DashboardLoadindErrorEvent dashboardLoadindErrorEvent = DashboardLoadindErrorEvent.builder()
                .dashboardId(dashboard.getId())
                .dashboardItemId(dashboardItem.getId())
                .informeWidgetItem(loadingErrorItem)
                .build();

        eventPublisher.publishEvent(dashboardLoadindErrorEvent);
        log.info("Dashboard {}: Loading item {} error", dashboard.getId(), dashboardItem.getId());
    }

}
