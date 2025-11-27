package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaGraficWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetBaseResource;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementació del servei EstadisticaGraficWidgetService per gestionar les operacions sobre widgets de Grafic d'estadístiques.
 * Aquesta classe extén BaseReadonlyResourceService, proporcionant funcionalitats estàndard de gestió de recursos de només lectura.
 *
 * Aquesta implementació utilitza Lombok per al registre del logger i es gestiona com un component de servei dins del context de Spring.
 *
 * L'objectiu d'aquest servei és facilitar les operacions associades amb els widgets de Grafic per estadístiques, basant-se en el model i l'entitat corresponent.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaGraficWidgetServiceImpl extends BaseMutableResourceService<EstadisticaGraficWidget, Long, EstadisticaGraficWidgetEntity> implements EstadisticaGraficWidgetService {

    @Autowired private EstadisticaGraficWidgetHelper estadisticaGraficWidgetHelper;
    @Autowired private EstadisticaWidgetHelper estadisticaWidgetHelper;
    @Autowired private AtributsVisualsHelper atributsVisualsHelper;

    @Override
    protected void beforeCreateSave(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        try {
            String atributsVisualsJson = atributsVisualsHelper.getAtributsVisualsJson(resource.getAtributsVisuals());
            entity.setAtributsVisualsJson(atributsVisualsJson);
        } catch (Exception e) {

            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotCreatedException(resource.getClass(), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void beforeUpdateSave(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        try {
            String atributsVisualsJson = atributsVisualsHelper.getAtributsVisualsJson(resource.getAtributsVisuals());
            entity.setAtributsVisualsJson(atributsVisualsJson);
        } catch (Exception e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void afterCreateSave(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    @Override
    protected void afterUpdateSave(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
        estadisticaWidgetHelper.clearDashboardWidgetCacheByWidget(entity.getId());
    }

    @Override
    protected void afterConversion(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {
        estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);
        estadisticaWidgetHelper.afterConversionGetDimensions(entity, resource);
        estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource);
        // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
        resource.setAtributsVisuals((AtributsVisualsGrafic) atributsVisualsHelper.getAtributsVisuals(entity));
    }

    @Override
    protected void completeResource(EstadisticaGraficWidget resource) {
        super.completeResource(resource);
        resource.setAppId(resource.getAplicacio().getId());
    }

    @Override
    public Map<String, Object> onChange(Long aLong, EstadisticaGraficWidget previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceFieldNotFoundException, AnswerRequiredException {
        HashMap<String, Object> changes = new HashMap<>();
        if (fieldName.equals(WidgetBaseResource.Fields.aplicacio) && !Objects.equals(previous.getAplicacio(), fieldValue)) {
            changes.put(WidgetBaseResource.Fields.dimensionsValor, null);
            changes.put(EstadisticaGraficWidget.Fields.indicador, null);
            changes.put(EstadisticaGraficWidget.Fields.descomposicioDimensio, null);
        }
        return changes;
    }
}
