package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.service.DashboardService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
public class DashboardServiceImpl extends BaseMutableResourceService<Dashboard, Long, DashboardEntity> implements DashboardService {

    @Autowired
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;

    public DashboardServiceImpl(ConsultaEstadisticaHelper consultaEstadisticaHelper) {
        super();
        this.consultaEstadisticaHelper = consultaEstadisticaHelper;
    }

    @PostConstruct
    public void init() {
        register(Dashboard.WIDGETS_REPORT, new DashboardServiceImpl.InformeWidgets());
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
            return dashboard.getItems().stream()
                    .map(item -> consultaEstadisticaHelper.getDadesWidget(item))
                    .collect(Collectors.toList());
        }

        private DashboardEntity getDashboard(String code, DashboardEntity entity) {
            DashboardEntity dashboard = entityRepository.findById(entity.getId())
                    .orElseThrow(() -> new ReportGenerationException(Dashboard.class, entity.getId(), code, "No existeix"));
            return dashboard;
        }

        @Override
        public void onChange(Serializable id, InformeWidgetParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, InformeWidgetParams target) {
        }

    }
}
