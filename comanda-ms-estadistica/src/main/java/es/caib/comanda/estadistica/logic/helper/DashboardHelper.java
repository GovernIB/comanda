package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.estadistica.persist.entity.dashboard.TemplateEstilsEntity;
import es.caib.comanda.estadistica.persist.repository.TemplateEstilsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardHelper {

    private final EstadisticaClientHelper estadisticaClientHelper;
    private final TemplateEstilsRepository templateEstilsRepository;

    public static final String ANSWER_CODE_APP_ID = "appId";
    public static final String ANSWER_CODE_ENTORN_ID = "entornId";

    public void completeResourceLogic(Dashboard resource) {
        resource.setAppId(Objects.nonNull(resource.getAplicacio()) ? resource.getAplicacio().getId() : null);
        resource.setEntornId(Objects.nonNull(resource.getEntorn()) ? resource.getEntorn().getId() : null);
    }

    public void afterConversionLogic(DashboardEntity entity, Dashboard resource) {
        afterConversionGetAppNom(entity, resource);
        afterConversionGetEntornNom(entity, resource);
    }

    /** Assigna el nom de l'aplicació a partir de l'appId **/
    private void afterConversionGetAppNom(DashboardEntity entity, Dashboard resource) {
        if (Objects.isNull(entity.getAppId())) {
            return;
        }

        try {
            App app = estadisticaClientHelper.appFindById(entity.getAppId());
            if (app != null) {
                resource.setAplicacio(ResourceReference.toResourceReference(app.getId(), app.getNom()));
            }
        } catch (Exception e) {
            log.error("Error obtenint el nom de l'aplicació amb id=" + entity.getAppId(), e);
        }
    }

    /** Assigna el nom de l'entorn a partir de l'entornId **/
    private void afterConversionGetEntornNom(DashboardEntity entity, Dashboard resource) {
        if (Objects.isNull(entity.getEntornId())) {
            return;
        }
        try {
            Entorn entorn = estadisticaClientHelper.entornById(entity.getEntornId());
            if (entorn != null) {
                resource.setEntorn(ResourceReference.toResourceReference(entorn.getId(), entorn.getNom()));
            }
        } catch (Exception e) {
            log.error("Error obtenint el nom de l'entorn amb id=" + entity.getEntornId(), e);
        }
    }

    public void beforeUpdateEntityLogic(DashboardEntity entity, Dashboard resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        beforeUpdateChangeEntornApp(entity, resource, answers);
        if (entity.getId() == null && entity.getTemplate() == null) {
            // Nou dashboard, assignar plantilla per defecte
            Optional<TemplateEstilsEntity> defaultTemplate = templateEstilsRepository.findByNom("Guia d'estils web");
            defaultTemplate.ifPresent(entity::setTemplate);
        }
        if (resource.getTemplateId() != null) {
            templateEstilsRepository.findById(resource.getTemplateId()).ifPresent(entity::setTemplate);
        }
    }

    private void beforeUpdateChangeEntornApp(DashboardEntity entity, Dashboard resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        if (entity.getItems().isEmpty()) {
            return;
        }
        if ((answers.containsKey(ANSWER_CODE_ENTORN_ID) && !answers.get(ANSWER_CODE_ENTORN_ID).getBooleanValue()) ||
            (answers.containsKey(ANSWER_CODE_APP_ID))) {
            throw new ResourceNotUpdatedException(
                Dashboard.class,
                entity.getId().toString(),
                I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.answer.refused"));
        }
        boolean canviAppId = !Objects.equals(entity.getAppId(), resource.getAppId()) && Objects.nonNull(resource.getAppId());
        boolean canviEntornId = !Objects.equals(entity.getEntornId(), resource.getEntornId()) && Objects.nonNull(resource.getEntornId());
        if (!canviAppId && !canviEntornId) {
            return;
        }
        Long newAppId = resource.getAppId();
        Long newEntornId = resource.getEntornId();
        EntornApp newEntornApp = (Objects.nonNull(newAppId) && Objects.nonNull(newEntornId)) ?
            estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(newAppId, newEntornId) : null;
        for (DashboardItemEntity item : entity.getItems()) {
            // Validamos si el widget tiene app compatible
            if (canviAppId && !answers.containsKey(ANSWER_CODE_APP_ID)) {
                EstadisticaWidgetEntity<?> widget = item.getWidget();
                if (widget != null && !Objects.equals(widget.getAppId(), newAppId)) {
                    throw new AnswerRequiredException(
                        Dashboard.class,
                        ANSWER_CODE_APP_ID,
                        I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.appId")
                    );
                }
            }
            //Actualizaremos su referencia a EntornApp.
            if (Objects.nonNull(newEntornApp)) {
                if (!Objects.equals(newEntornApp.getId(), item.getEntornId())) {
                    item.setEntornId(newEntornApp.getId());
                }
            } else {
                EntornApp itemEntornApp = estadisticaClientHelper.entornAppFindById(item.getEntornId());
                Long itemAppId = Objects.nonNull(newAppId) ? newAppId : itemEntornApp.getApp().getId();
                Long itemEntornId = Objects.nonNull(newEntornId) ? newEntornId : itemEntornApp.getEntorn().getId();
                EntornApp newItemEntornApp = estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(itemAppId, itemEntornId);
                if (Objects.nonNull(newItemEntornApp)) {
                    item.setEntornId(newItemEntornApp.getId());
                } else if (!answers.containsKey(ANSWER_CODE_ENTORN_ID)) {
                    throw new AnswerRequiredException(
                        Dashboard.class,
                        ANSWER_CODE_ENTORN_ID,
                        I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.entornId"),
                        null
                    );
                }
            }
        }
    }

    public static class CloneDashboardAction implements BaseMutableResourceService.ActionExecutor<DashboardEntity, Dashboard, Dashboard> {

        private final EstadisticaClientHelper estadisticaClientHelper;
        private final DashboardRepository dashboardRepository;
        private final DashboardTitolRepository dashboardTitolRepository;
        private final DashboardItemRepository dashboardItemRepository;

        public CloneDashboardAction(EstadisticaClientHelper estadisticaClientHelper, DashboardRepository dashboardRepository, DashboardTitolRepository dashboardTitolRepository, DashboardItemRepository dashboardItemRepository) {
            this.estadisticaClientHelper = estadisticaClientHelper;
            this.dashboardRepository = dashboardRepository;
            this.dashboardTitolRepository = dashboardTitolRepository;
            this.dashboardItemRepository = dashboardItemRepository;
        }

        @Override
        public Dashboard exec(String code, DashboardEntity entity, Dashboard params) throws ActionExecutionException {
            DashboardEntity newDashboard = new DashboardEntity();
            if (Objects.nonNull(params)) {
                newDashboard.setTitol(params.getTitol());
                newDashboard.setDescripcio(params.getDescripcio());
                newDashboard.setAppId(Objects.nonNull(params.getAplicacio()) ? params.getAplicacio().getId() : params.getAppId());
                newDashboard.setEntornId(Objects.nonNull(params.getEntorn()) ? params.getEntorn().getId() : params.getEntornId());
            } else {//Si no nos envian body usaremos los valores de la propia entidad.
                newDashboard.setTitol(entity.getTitol() + " (Copia)");//El nombre debe ser diferente para respetar la UK
                newDashboard.setDescripcio(entity.getDescripcio());
                newDashboard.setAppId(entity.getAppId());
                newDashboard.setEntornId(entity.getEntornId());
            }
            List<DashboardTitolEntity> clonedTitols = getClonedTitulos(entity, newDashboard);
            List<DashboardItemEntity> clonedItems = getClonedItem(entity, newDashboard);
            dashboardRepository.save(newDashboard);
            dashboardTitolRepository.saveAll(clonedTitols);
            dashboardItemRepository.saveAll(clonedItems);
            return null;
        }

        private List<DashboardTitolEntity> getClonedTitulos(DashboardEntity originalDashboard, DashboardEntity newDashboard) {
            List<DashboardTitolEntity> clonedTitols = new ArrayList<>();
            if (originalDashboard.getTitols() != null) {
                for (DashboardTitolEntity original : originalDashboard.getTitols()) {
                    DashboardTitolEntity clone = new DashboardTitolEntity();
                    clone.setDashboard(newDashboard);
                    clone.setTitol(original.getTitol());
                    clone.setSubtitol(original.getSubtitol());
                    clone.setPosX(original.getPosX());
                    clone.setPosY(original.getPosY());
                    clone.setWidth(original.getWidth());
                    clone.setHeight(original.getHeight());
                    clone.setColorTitol(original.getColorTitol());
                    clone.setMidaFontTitol(original.getMidaFontTitol());
                    clone.setColorSubtitol(original.getColorSubtitol());
                    clone.setMidaFontSubtitol(original.getMidaFontSubtitol());
                    clone.setColorFons(original.getColorFons());
                    clone.setMostrarVora(original.getMostrarVora());
                    clone.setColorVora(original.getColorVora());
                    clone.setAmpleVora(original.getAmpleVora());
                    clonedTitols.add(clone);
                }
            }
            newDashboard.setTitols(clonedTitols);
            return clonedTitols;
        }

        private List<DashboardItemEntity> getClonedItem(DashboardEntity originalDashboard, DashboardEntity newDashboard) {
            List<DashboardItemEntity> clonedItems = new ArrayList<>();
            if (originalDashboard.getItems() != null) {
                boolean canviAppId = !Objects.equals(originalDashboard.getAppId(), newDashboard.getAppId()) && Objects.nonNull(newDashboard.getAppId());
                boolean canviEntornId = !Objects.equals(originalDashboard.getEntornId(), newDashboard.getEntornId()) && Objects.nonNull(newDashboard.getEntornId());
                Long newAppId = newDashboard.getAppId();
                Long newEntornId = newDashboard.getEntornId();
                EntornApp newEntornApp = (Objects.nonNull(newAppId) && Objects.nonNull(newEntornId)) ?
                    estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(newAppId, newEntornId) : null;
                for (DashboardItemEntity original : originalDashboard.getItems()) {
                    DashboardItemEntity clone = new DashboardItemEntity();
                    clone.setDashboard(newDashboard);
                    clone.setWidget(original.getWidget());
                    clone.setPosX(original.getPosX());
                    clone.setPosY(original.getPosY());
                    clone.setWidth(original.getWidth());
                    clone.setHeight(original.getHeight());
                    clone.setAtributsVisualsJson(original.getAtributsVisualsJson());
                    if (!Objects.equals(original.getWidget().getAppId(), newAppId)){
                        throw new ActionExecutionException(
                            Dashboard.class,
                            originalDashboard.getId(),
                            Dashboard.CLONE_ACTION,
                            I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.appId")
                        );
                    }
                    Long resolvedEntornId;
                    if (!canviAppId && !canviEntornId) {
                        resolvedEntornId = original.getEntornId();
                    } else if (newEntornApp != null) {
                        resolvedEntornId = newEntornApp.getId();
                    } else {
                        EntornApp itemEntornApp = estadisticaClientHelper.entornAppFindById(original.getEntornId());
                        Long itemAppId = Objects.nonNull(newAppId) ? newAppId : itemEntornApp.getApp().getId();
                        Long itemEntornId = Objects.nonNull(newEntornId) ? newEntornId : itemEntornApp.getEntorn().getId();
                        EntornApp newItemEntornApp = estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(itemAppId, itemEntornId);
                        if (Objects.isNull(newItemEntornApp)) {
                            throw new ActionExecutionException(
                                Dashboard.class,
                                originalDashboard.getId(),
                                Dashboard.CLONE_ACTION,
                                I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.action.error.appId")
                            );
                        }
                        resolvedEntornId = newItemEntornApp.getId();
                    }
                    clone.setEntornId(resolvedEntornId);
                    clonedItems.add(clone);
                }
            }
            newDashboard.setItems(clonedItems);
            return clonedItems;
        }

        @Override
        public void onChange(Serializable id, Dashboard previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Dashboard target) {
        }

    }

}
