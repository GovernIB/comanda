package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaAsyncHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.service.DashboardService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els dashboards.
 * Aquesta classe extend BaseReadonlyResourceService i implementa la interfície DashboardService.
 *
 * Proporciona funcionalitats específiques per treballar amb el model de dades Dashboard,
 * interactuant amb l'entitat persistent DashboardEntity.
 *
 * Aquesta classe utilitza anotacions de Spring per ser detectada com a servei,
 * i registra logs mitjançant Lombok.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardServiceImpl extends BaseMutableResourceService<Dashboard, Long, DashboardEntity> implements DashboardService {

    private final ConsultaEstadisticaHelper consultaEstadisticaHelper;
    private final ConsultaEstadisticaAsyncHelper consultaEstadisticaAsyncHelper;

    @PostConstruct
    public void init() {
        register(Dashboard.WIDGETS_REPORT, new DashboardServiceImpl.InformeWidgets());
    }

    private DashboardEntity getDashboard(String code, DashboardEntity entity) {
        DashboardEntity dashboard = entityRepository.findById(entity.getId())
                .orElseThrow(() -> new ReportGenerationException(Dashboard.class, entity.getId(), code, "No existeix"));
        return dashboard;
    }

    // REPORT PER OBTENIR EMPLENAR I WIDGETS
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class InformeWidgets implements ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> {

        @Override
        public List<InformeWidgetItem> generateData(
                String code,
                DashboardEntity entity,
                InformeWidgetParams params) throws ReportGenerationException {

            DashboardEntity dashboard = getDashboard(code, entity);
            List<InformeWidgetItem> dashboardItems = dashboard.getItems().stream()
                    .map(item -> {
                        InformeWidgetItem informeItem = InformeWidgetItem.builder()
                                .dashboardItemId(item.getId())
                                .titol(item.getWidget() != null ? item.getWidget().getTitol() : null)
                                .tipus(consultaEstadisticaHelper.determineWidgetType(item))
                                .posX(item.getPosX())
                                .posY(item.getPosY())
                                .width(item.getWidth())
                                .height(item.getHeight())
                                .loading(true)
                                .build();
                        return informeItem;
                    })
                    .collect(Collectors.toList());
            dashboard.getItems().parallelStream().forEach(dashboardItem -> {
                try {
                    log.info("Carregant dades del widget {}...", dashboardItem.getId());
                    consultaEstadisticaAsyncHelper.generateAsyncData(dashboardItem);
                } catch (Exception e) {
                    log.error("Error generant informe widget. Item {}: {}", dashboardItem.getId(), e.getMessage(), e);
                }
            });
            log.info("Dashboard {}: {} items generated", entity.getId(), dashboardItems.size());
            return dashboardItems;
        }

        @Override
        public void onChange(Serializable id, InformeWidgetParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, InformeWidgetParams target) {
        }

    }
}
