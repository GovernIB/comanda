package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementació de la interfície EstadisticaGraficWidgetService per gestionar el servei associat als widgets gràfics d'estadístiques.
 * Aquesta classe estén BaseReadonlyResourceService per proporcionar operacions bàsiques de lectura sobre l'entitat
 * EstadisticaGraficWidgetEntity.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaGraficWidgetServiceImpl extends BaseMutableResourceService<EstadisticaGraficWidget, Long, EstadisticaGraficWidgetEntity> implements EstadisticaGraficWidgetService {

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
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    @Override
    protected void afterCreateSave(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    @Override
    protected void afterUpdateSave(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    @Override
    protected void afterConversion(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {
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

}