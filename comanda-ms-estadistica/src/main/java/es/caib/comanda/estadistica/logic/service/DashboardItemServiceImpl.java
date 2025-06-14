package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.service.DashboardItemService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
public class DashboardItemServiceImpl extends BaseMutableResourceService<DashboardItem, Long, DashboardItemEntity> implements DashboardItemService {

    @Autowired
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Autowired
    private AtributsVisualsHelper atributsVisualsHelper;
    @Autowired
    private EstadisticaWidgetHelper estadisticaWidgetHelper;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    public void init() {
        register(DashboardItem.WIDGET_REPORT, new InformeWidget());
    }

    @Override
    protected void beforeCreateSave(DashboardItemEntity entity, DashboardItem resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        try {
            String atributsVisualsJson = atributsVisualsHelper.getAtributsVisualsJson(resource.getAtributsVisuals());
            entity.setAtributsVisualsJson(atributsVisualsJson);
        } catch (Exception e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotCreatedException(resource.getClass(), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void beforeUpdateSave(DashboardItemEntity entity, DashboardItem resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        try {
            String atributsVisualsJson = atributsVisualsHelper.getAtributsVisualsJson(resource.getAtributsVisuals());
            entity.setAtributsVisualsJson(atributsVisualsJson);
        } catch (Exception e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void afterUpdateSave(DashboardItemEntity entity, DashboardItem resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        estadisticaWidgetHelper.clearDashboardWidgetCache(entity.getId());
    }

    @Override
    protected void afterConversion(DashboardItemEntity entity, DashboardItem resource) {
        // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
        resource.setAtributsVisuals(atributsVisualsHelper.getAtributsVisuals(entity));
    }


    // REPORT PER OBTENIR I EMPLENAR WIDGETS
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class InformeWidget implements ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> {

        @Override
        public List<InformeWidgetItem> generateData(
                String code,
                DashboardItemEntity entity,
                InformeWidgetParams params) throws ReportGenerationException {

            DashboardItemEntity dashboardItem = getDashboardItem(code, entity);
            InformeWidgetItem item;
            try {
                item = consultaEstadisticaHelper.getDadesWidget(dashboardItem);
            } catch (Exception e) {
                log.error("Error generant informe widget. Item {}: {}", dashboardItem.getId(), e.getMessage(), e);
                item = InformeWidgetItem.builder()
                        .dashboardItemId(dashboardItem.getId())
                        .titol(dashboardItem.getWidget() != null ? dashboardItem.getWidget().getTitol() : null)
                        .tipus(consultaEstadisticaHelper.determineWidgetType(dashboardItem))
                        .posX(dashboardItem.getPosX())
                        .posY(dashboardItem.getPosY())
                        .width(dashboardItem.getWidth())
                        .height(dashboardItem.getHeight())
                        .error(true)
                        .errorMsg("Error processing item " + dashboardItem.getId() + ": " + e.getMessage())
                        .errorTrace(ExceptionUtils.getStackTrace(e))
                        .build();
            }

            return List.of(item);
        }

        private DashboardItemEntity getDashboardItem(String code, DashboardItemEntity entity) {
            DashboardItemEntity dashboardItem = entityRepository.findById(entity.getId())
                    .orElseThrow(() -> new ReportGenerationException(DashboardItem.class, entity.getId(), code, "No existeix"));
            return dashboardItem;
        }

        @Override
        public void onChange(Serializable id, InformeWidgetParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, InformeWidgetParams target) {
        }

    }

}
