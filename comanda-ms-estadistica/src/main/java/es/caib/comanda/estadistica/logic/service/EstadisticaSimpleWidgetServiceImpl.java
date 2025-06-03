package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaSimpleWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementació del servei per gestionar widgets d'estadística simple.
 * Aquesta classe proporciona funcionalitats de només lectura per interactuar amb dades de widgets d'estadística simple.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaSimpleWidgetServiceImpl extends BaseMutableResourceService<EstadisticaSimpleWidget, Long, EstadisticaSimpleWidgetEntity> implements EstadisticaSimpleWidgetService {

    @Autowired private EstadisticaClientHelper estadisticaClientHelper;
    @Autowired private EstadisticaWidgetHelper estadisticaWidgetHelper;
    @Autowired private EstadisticaSimpleWidgetHelper estadisticaSimpleWidgetHelper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void beforeCreateEntity(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        // Convertir els atributs visuals a JSON i guardar-los a l'entitat
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotCreatedException(resource.getClass(), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void afterCreateSave(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    @Override
    protected void beforeUpdateEntity(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);
        // Convertir els atributs visuals a JSON i guardar-los a l'entitat
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void beforeUpdateSave(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    private void convertAndSetAtributsVisuals(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) throws JsonProcessingException {
        AtributsVisualsSimple atributsVisuals = resource.getAtributsVisuals();
        if (atributsVisuals != null) {
            entity.setAtributsVisuals(objectMapper.writeValueAsString(atributsVisuals));
        }
    }

    @Override
    protected void afterConversion(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) {
        estadisticaSimpleWidgetHelper.afterCoversionGetIndicadorTaulaAtributes(entity, resource);
        estadisticaWidgetHelper.afterConversionGetDimensions(entity, resource);
        estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource);
        // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
        if (entity.getAtributsVisuals() != null && !entity.getAtributsVisuals().isEmpty()) {
            try {
                AtributsVisualsSimple atributsVisuals = objectMapper.readValue(entity.getAtributsVisuals(), AtributsVisualsSimple.class);
                resource.setAtributsVisuals(atributsVisuals);
            } catch (JsonProcessingException e) {
                log.error("Error convertint JSON a atributs visuals", e);
            }
        }
    }
}