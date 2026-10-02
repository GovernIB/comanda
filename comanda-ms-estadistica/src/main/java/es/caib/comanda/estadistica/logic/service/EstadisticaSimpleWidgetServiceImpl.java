package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardPermisosHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaSimpleWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetBaseResource;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementació del servei per gestionar widgets d'estadística simple.
 * Aquesta classe proporciona funcionalitats de només lectura per interactuar amb dades de widgets d'estadística simple.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaSimpleWidgetServiceImpl extends BaseMutableResourceService<EstadisticaSimpleWidget, Long, EstadisticaSimpleWidgetEntity> implements EstadisticaSimpleWidgetService {

    @Autowired private EstadisticaWidgetHelper estadisticaWidgetHelper;
    @Autowired private EstadisticaSimpleWidgetHelper estadisticaSimpleWidgetHelper;
    @Autowired private AtributsVisualsHelper atributsVisualsHelper;
    @Autowired private DashboardPermisosHelper dashboardPermisosHelper;

    @Override
    protected String additionalSpringFilter(
        String currentSpringFilter,
        String[] namedQueries) {
        return dashboardPermisosHelper.buildWidgetFilter(currentSpringFilter, namedQueries);
    }

    @Override
    protected Specification<EstadisticaSimpleWidgetEntity> namedFilterToSpecification(String name) {
        return estadisticaWidgetHelper.namedFilterToSpecification(name);
    }

    @Override
    protected void beforeCreateEntity(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        Long appId = (resource != null && resource.getAppId() != null)
                ? resource.getAppId()
                : (resource != null && resource.getAplicacio() != null ? resource.getAplicacio().getId() : (entity != null ? entity.getAppId() : null));
        dashboardPermisosHelper.checkCanCreateWidget(appId, I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.service.EstadisticaWidget.permisos.crear"));
    }

    @Override
    protected void beforeUpdateEntity(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        dashboardPermisosHelper.checkCanDesignWidget(entity, I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.service.EstadisticaWidget.permisos.modificar"));
        Long targetAppId = (resource != null && resource.getAppId() != null)
                ? resource.getAppId()
                : (resource != null && resource.getAplicacio() != null ? resource.getAplicacio().getId() : null);
        if (targetAppId != null && entity != null && !Objects.equals(targetAppId, entity.getAppId())) {
            dashboardPermisosHelper.checkCanCreateWidget(targetAppId, I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.service.EstadisticaWidget.permisos.modificar"));
        }
    }

    @Override
    protected void beforeDelete(EstadisticaSimpleWidgetEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        dashboardPermisosHelper.checkCanDeleteWidget(entity, I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.service.EstadisticaWidget.permisos.eliminar"));
    }

    @Override
    protected void beforeCreateSave(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        try {
            String atributsVisualsJson = atributsVisualsHelper.getAtributsVisualsJson(resource.getAtributsVisuals());
            entity.setAtributsVisualsJson(atributsVisualsJson);
        } catch (Exception e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotCreatedException(resource.getClass(), "Error convertint atributs visuals a JSON");
        }
        // S'actualitza l'indicador abans del save/flush per evitar problemes de bloqueig optimista (OptimisticLockException)
        // en entitats associades @OneToOne amb cascade=ALL en fer detach/merge.
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);
    }

    @Override
    protected void beforeUpdateSave(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        try {
            String atributsVisualsJson = atributsVisualsHelper.getAtributsVisualsJson(resource.getAtributsVisuals());
            entity.setAtributsVisualsJson(atributsVisualsJson);
        } catch (Exception e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), "Error convertint atributs visuals a JSON");
        }
        // S'actualitza l'indicador abans del save/flush per evitar problemes de bloqueig optimista (OptimisticLockException)
        // en entitats associades @OneToOne amb cascade=ALL en fer detach/merge.
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);
    }

    @Override
    protected void afterCreateSave(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
    }

    @Override
    protected void afterUpdateSave(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);
        estadisticaWidgetHelper.clearDashboardWidgetCacheByWidget(entity.getId());
    }

    @Override
    protected void afterConversion(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) {
        estadisticaSimpleWidgetHelper.afterCoversionGetIndicadorTaulaAtributes(entity, resource);
        estadisticaWidgetHelper.afterConversionGetDimensions(entity, resource);
        estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource);
        // Convertir el JSON d'atributs visuals a objectes i assignar-los al recurs
        resource.setAtributsVisuals((AtributsVisualsSimple) atributsVisualsHelper.getAtributsVisuals(entity));
    }

    @Override
    protected void completeResource(EstadisticaSimpleWidget resource) {
        super.completeResource(resource);
        resource.setAppId(resource.getAplicacio().getId());
    }

    @Override
    protected void updateEntityWithResource(
            EstadisticaSimpleWidgetEntity entity,
            EstadisticaSimpleWidget resource,
            Map<String, Persistable<?>> referencedEntities) {
        IndicadorTaulaEntity prevIndicador = entity.getIndicadorInfo();
        super.updateEntityWithResource(entity, resource, referencedEntities);
        // Evitem que updateEntityWithResource posi a null el camp indicadorInfo (ja que IndicadorTaulaEntity
        // és Persistable però no s'envia com a referència des del frontend, sinó com a camps plans).
        // Preservar la instància existent evita trencar la relació @OneToOne en memòria i estalvia
        // una consulta SELECT addicional per refer el vincle abans de guardar.
        if (prevIndicador != null) {
            entity.setIndicadorInfo(prevIndicador);
        }
    }

    @Override
    protected EstadisticaSimpleWidgetEntity entitySaveFlushAndRefresh(EstadisticaSimpleWidgetEntity entity) {
        // S'evita el refresh(saved) de BaseMutableResourceService. En servidors amb JTA (JBoss EAP),
        // executar refresh() entremig de canvis previs (indicador a beforeUpdateSave) i canvis posteriors
        // (dimensions a afterUpdateSave) recarrega l'estat des de la BD desincronitzant els snapshots
        // de versió (@Version) de la relació @OneToOne, provocant un error StaleStateException al flush final.
        return entityRepository.saveAndFlush(entity);
    }

    @Override
    protected EstadisticaSimpleWidget entityDetachConvertAndMerge(
            EstadisticaSimpleWidgetEntity entity,
            Map<String, AnswerRequiredException.AnswerValue> answers,
            boolean create) {
        // S'evita el cicle detach() + merge() de BaseMutableResourceService per evitar problemes
        // de bloqueig optimista (StaleStateException / OptimisticLockException) amb la relació @OneToOne
        // d'indicadorInfo en servidors d'aplicacions amb JTA (JBoss EAP).
        EstadisticaSimpleWidget response = entityToResource(entity);
        entityAfterMergeLogic(response, entity, answers, create);
        return response;
    }

    @Override
    public Map<String, Object> onChange(Long aLong, EstadisticaSimpleWidget previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceFieldNotFoundException, AnswerRequiredException {
        HashMap<String, Object> changes = new HashMap<>();
        if (fieldName.equals(WidgetBaseResource.Fields.aplicacio) && !Objects.equals(previous.getAplicacio(), fieldValue)) {
            changes.put(WidgetBaseResource.Fields.dimensionsValor, null);
            changes.put(EstadisticaSimpleWidget.Fields.indicador, null);
        }
        return changes;
    }
}
