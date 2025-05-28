package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaGraficWidgetEntity;
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

    @Autowired
    private EstadisticaClientHelper estadisticaClientHelper;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void beforeCreateEntity(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        // Convertir els atributs visuals a JSON i guardar-los a l'entitat
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotCreatedException(resource.getClass(), "Error convertint atributs visuals a JSON");
        }
    }
    
    @Override
    protected void beforeUpdateEntity(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
    }

    private void convertAndSetAtributsVisuals(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) throws JsonProcessingException {
        AtributsVisualsGrafic atributsVisuals = resource.getAtributsVisuals();
        if (atributsVisuals != null) {
            entity.setAtributsVisuals(objectMapper.writeValueAsString(atributsVisuals));
        }
    }

    @Override
    protected void afterConversion(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {
        try {
            var app = estadisticaClientHelper.appFindById(entity.getAppId());
            if (app != null) {
                resource.setAplicacioNom(app.getNom());
            }
            
            // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
            if (entity.getAtributsVisuals() != null && !entity.getAtributsVisuals().isEmpty()) {
                try {
                    AtributsVisualsGrafic atributsVisuals = objectMapper.readValue(entity.getAtributsVisuals(), AtributsVisualsGrafic.class);
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