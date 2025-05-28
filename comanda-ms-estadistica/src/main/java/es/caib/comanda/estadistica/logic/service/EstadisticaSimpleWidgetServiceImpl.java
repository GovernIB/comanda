package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaSimpleWidgetEntity;
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

    @Autowired
    private EstadisticaClientHelper estadisticaClientHelper;

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
    protected void beforeUpdateEntity(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        // Convertir els atributs visuals a JSON i guardar-los a l'entitat
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
    }

    private void convertAndSetAtributsVisuals(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) throws JsonProcessingException {
        AtributsVisualsSimple atributsVisuals = resource.getAtributsVisuals();
        if (atributsVisuals != null) {
            entity.setAtributsVisuals(objectMapper.writeValueAsString(atributsVisuals));
        }
    }

    @Override
    protected void afterConversion(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) {
        try {
            var app = estadisticaClientHelper.appFindById(entity.getAppId());
            if (app != null) {
                resource.setAplicacioNom(app.getNom());
            }
            
            // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
            if (entity.getAtributsVisuals() != null && !entity.getAtributsVisuals().isEmpty()) {
                try {
                    AtributsVisualsSimple atributsVisuals = objectMapper.readValue(entity.getAtributsVisuals(), AtributsVisualsSimple.class);
                    resource.setAtributsVisuals(atributsVisuals);
                } catch (JsonProcessingException e) {
                    log.error("Error convertint JSON a atributs visuals", e);
                }
            }
        } catch (Exception e) {
            log.error("Error obtenint el nom de l'aplicació amb id=" + entity.getAppId(), e);
        }
    }
}