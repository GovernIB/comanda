package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaTaulaWidgetService;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementació del servei EstadisticaTaulaWidgetService per gestionar les operacions sobre widgets de taula d'estadístiques.
 * Aquesta classe extén BaseReadonlyResourceService, proporcionant funcionalitats estàndard de gestió de recursos de només lectura.
 *
 * Aquesta implementació utilitza Lombok per al registre del logger i es gestiona com un component de servei dins del context de Spring.
 *
 * L'objectiu d'aquest servei és facilitar les operacions associades amb els widgets de taula per estadístiques, basant-se en el model i l'entitat corresponent.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaTaulaWidgetServiceImpl extends BaseMutableResourceService<EstadisticaTaulaWidget, Long, EstadisticaTaulaWidgetEntity> implements EstadisticaTaulaWidgetService {

    @Autowired private EstadisticaClientHelper estadisticaClientHelper;
    @Autowired private EstadisticaWidgetHelper estadisticaWidgetHelper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void beforeCreateEntity(EstadisticaTaulaWidgetEntity entity, EstadisticaTaulaWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        // Convertir els atributs visuals a JSON i guardar-los a l'entitat
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotCreatedException(resource.getClass(), "Error convertint atributs visuals a JSON");
        }
    }

    @Override
    protected void beforeUpdateEntity(EstadisticaTaulaWidgetEntity entity, EstadisticaTaulaWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        // Convertir els atributs visuals a JSON i guardar-los a l'entitat
        try {
            convertAndSetAtributsVisuals(entity, resource);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
    }

    private void convertAndSetAtributsVisuals(EstadisticaTaulaWidgetEntity entity, EstadisticaTaulaWidget resource) throws JsonProcessingException {
        AtributsVisualsTaula atributsVisuals = resource.getAtributsVisuals();
        if (atributsVisuals != null) {
            entity.setAtributsVisuals(objectMapper.writeValueAsString(atributsVisuals));
        }
    }


    @Override
    protected void afterConversion(EstadisticaTaulaWidgetEntity entity, EstadisticaTaulaWidget resource) {
        estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource);
        // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
        if (entity.getAtributsVisuals() != null && !entity.getAtributsVisuals().isEmpty()) {
            try {
                AtributsVisualsTaula atributsVisuals = objectMapper.readValue(entity.getAtributsVisuals(), AtributsVisualsTaula.class);
                resource.setAtributsVisuals(atributsVisuals);
            } catch (JsonProcessingException e) {
                log.error("Error convertint JSON a atributs visuals", e);
            }
        }
    }
}
