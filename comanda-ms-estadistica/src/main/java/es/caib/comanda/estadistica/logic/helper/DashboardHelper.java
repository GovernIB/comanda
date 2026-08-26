package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.estadistica.persist.repository.PlantillaRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardHelper {

    private final EstadisticaClientHelper estadisticaClientHelper;

    public static final String ANSWER_CODE_APP_ID = "appId";
    public static final String ANSWER_CODE_ENTORN_ID = "entornId";

    public void completeResourceLogic(Dashboard resource) {
        resource.setAppId(Objects.nonNull(resource.getAplicacio()) ? resource.getAplicacio().getId() : null);
        resource.setEntornId(Objects.nonNull(resource.getEntorn()) ? resource.getEntorn().getId() : null);
    }

    public void afterConversionLogic(DashboardEntity entity, Dashboard resource) {
        if (Objects.nonNull(entity.getAppId())) {
            afterConversionGetAppNom(entity, resource);
        }
        if (Objects.nonNull(entity.getEntornId())) {
            afterConversionGetEntornNom(entity, resource);
        }
    }

    /** Assigna el nom de l'aplicació a partir de l'appId **/
    private void afterConversionGetAppNom(DashboardEntity entity, Dashboard resource) {
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
        private final PlantillaRepository plantillaRepository;
        private final EstadisticaWidgetRepository estadisticaWidgetRepository;
        private final es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper;

        public CloneDashboardAction(
                EstadisticaClientHelper estadisticaClientHelper,
                DashboardRepository dashboardRepository,
                DashboardTitolRepository dashboardTitolRepository,
                DashboardItemRepository dashboardItemRepository,
                PlantillaRepository plantillaRepository,
                EstadisticaWidgetRepository estadisticaWidgetRepository,
                es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper) {
            this.estadisticaClientHelper = estadisticaClientHelper;
            this.dashboardRepository = dashboardRepository;
            this.dashboardTitolRepository = dashboardTitolRepository;
            this.dashboardItemRepository = dashboardItemRepository;
            this.plantillaRepository = plantillaRepository;
            this.estadisticaWidgetRepository = estadisticaWidgetRepository;
            this.dashboardClonerMapper = dashboardClonerMapper;
        }

        @Override
        public Dashboard exec(String code, DashboardEntity entity, Dashboard params) throws ActionExecutionException {
            DashboardEntity newDashboard = new DashboardEntity();
            if (Objects.nonNull(params)) {
                newDashboard.setTitol(params.getTitol());
                newDashboard.setDescripcio(params.getDescripcio());
                newDashboard.setAppId(Objects.nonNull(params.getAplicacio()) ? params.getAplicacio().getId() : params.getAppId());
                newDashboard.setEntornId(Objects.nonNull(params.getEntorn()) ? params.getEntorn().getId() : params.getEntornId());
                if (Objects.nonNull(params.getPlantilla()) && Objects.nonNull(params.getPlantilla().getId())) {
                    newDashboard.setPlantilla(plantillaRepository.findById(params.getPlantilla().getId()).orElse(null));
                }
            } else {//Si no nos envian body usaremos los valores de la propia entidad.
                newDashboard.setTitol(entity.getTitol() + " (Copia)");//El nombre debe ser diferente para respetar la UK
                newDashboard.setDescripcio(entity.getDescripcio());
                newDashboard.setAppId(entity.getAppId());
                newDashboard.setEntornId(entity.getEntornId());
                newDashboard.setPlantilla(entity.getPlantilla());
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
                    DashboardTitolEntity clone = dashboardClonerMapper.cloneTitol(original);
                    clone.setDashboard(newDashboard);
                    clonedTitols.add(clone);
                }
            }
            newDashboard.setTitols(clonedTitols);
            return clonedTitols;
        }

        private List<DashboardItemEntity> getClonedItem(DashboardEntity originalDashboard, DashboardEntity newDashboard) {
            List<DashboardItemEntity> clonedItems = new ArrayList<>();
            Map<Long, EstadisticaWidgetEntity> clonedWidgetsMap = new HashMap<>();
            if (originalDashboard.getItems() != null) {
                boolean canviAppId = !Objects.equals(originalDashboard.getAppId(), newDashboard.getAppId()) && Objects.nonNull(newDashboard.getAppId());
                boolean canviEntornId = !Objects.equals(originalDashboard.getEntornId(), newDashboard.getEntornId()) && Objects.nonNull(newDashboard.getEntornId());
                Long newAppId = newDashboard.getAppId();
                Long newEntornId = newDashboard.getEntornId();
                EntornApp newEntornApp = (Objects.nonNull(newAppId) && Objects.nonNull(newEntornId)) ?
                    estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(newAppId, newEntornId) : null;
                for (DashboardItemEntity original : originalDashboard.getItems()) {
                    DashboardItemEntity clone = dashboardClonerMapper.cloneItem(original);
                    clone.setDashboard(newDashboard);
                    if (original.getWidget() != null) {
                        if (newAppId != null && !Objects.equals(original.getWidget().getAppId(), newAppId)){
                            throw new ActionExecutionException(
                                Dashboard.class,
                                originalDashboard.getId(),
                                Dashboard.CLONE_ACTION,
                                I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.appId")
                            );
                        }
                        EstadisticaWidgetEntity clonedWidget = cloneWidget(original.getWidget(), newAppId, clonedWidgetsMap);
                        clone.setWidget(clonedWidget);
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

        private EstadisticaWidgetEntity cloneWidget(
                EstadisticaWidgetEntity original,
                Long targetAppId,
                Map<Long, EstadisticaWidgetEntity> clonedWidgetsMap) {
            if (original == null) {
                return null;
            }
            if (original.getId() != null && clonedWidgetsMap.containsKey(original.getId())) {
                return clonedWidgetsMap.get(original.getId());
            }
            Long appId = targetAppId != null ? targetAppId : original.getAppId();
            String newTitol = getWidgetNewTitol(original.getTitol(), appId);

            EstadisticaWidgetEntity clone;
            if (original instanceof EstadisticaSimpleWidgetEntity) {
                EstadisticaSimpleWidgetEntity originalSimple = (EstadisticaSimpleWidgetEntity) original;
                EstadisticaSimpleWidgetEntity cloneSimple = dashboardClonerMapper.cloneSimpleWidget(originalSimple);
                cloneSimple.setTitol(newTitol);
                cloneSimple.setAppId(appId);
                if (originalSimple.getIndicadorInfo() != null) {
                    IndicadorTaulaEntity indClone = dashboardClonerMapper.cloneIndicadorTaula(originalSimple.getIndicadorInfo());
                    indClone.setWidget(cloneSimple);
                    cloneSimple.setIndicadorInfo(indClone);
                }
                clone = cloneSimple;
            } else if (original instanceof EstadisticaGraficWidgetEntity) {
                EstadisticaGraficWidgetEntity originalGrafic = (EstadisticaGraficWidgetEntity) original;
                EstadisticaGraficWidgetEntity cloneGrafic = dashboardClonerMapper.cloneGraficWidget(originalGrafic);
                cloneGrafic.setTitol(newTitol);
                cloneGrafic.setAppId(appId);
                if (originalGrafic.getIndicadorsInfo() != null) {
                    List<IndicadorTaulaEntity> clonedIndicadors = new ArrayList<>();
                    for (IndicadorTaulaEntity ind : originalGrafic.getIndicadorsInfo()) {
                        IndicadorTaulaEntity indClone = dashboardClonerMapper.cloneIndicadorTaula(ind);
                        indClone.setWidget(cloneGrafic);
                        clonedIndicadors.add(indClone);
                    }
                    cloneGrafic.setIndicadorsInfo(clonedIndicadors);
                }
                clone = cloneGrafic;
            } else if (original instanceof EstadisticaTaulaWidgetEntity) {
                EstadisticaTaulaWidgetEntity originalTaula = (EstadisticaTaulaWidgetEntity) original;
                EstadisticaTaulaWidgetEntity cloneTaula = dashboardClonerMapper.cloneTaulaWidget(originalTaula);
                cloneTaula.setTitol(newTitol);
                cloneTaula.setAppId(appId);
                if (originalTaula.getColumnes() != null) {
                    List<IndicadorTaulaEntity> clonedColumnes = new ArrayList<>();
                    for (IndicadorTaulaEntity col : originalTaula.getColumnes()) {
                        IndicadorTaulaEntity colClone = dashboardClonerMapper.cloneIndicadorTaula(col);
                        colClone.setWidget(cloneTaula);
                        clonedColumnes.add(colClone);
                    }
                    cloneTaula.setColumnes(clonedColumnes);
                }
                clone = cloneTaula;
            } else {
                throw new IllegalArgumentException("Tipus de widget desconegut: " + original.getClass().getName());
            }

            estadisticaWidgetRepository.save(clone);
            if (original.getId() != null) {
                clonedWidgetsMap.put(original.getId(), clone);
            }
            return clone;
        }

        private static final int MAX_TITOL_TRIES = 1000;

        private String getWidgetNewTitol(String originalTitol, Long appId) {
            String candidate = originalTitol;
            int counter = 1;
            while (appId != null && estadisticaWidgetRepository.findByAppIdAndTitol(appId, candidate) != null) {
                if (counter > MAX_TITOL_TRIES) {
                    throw new IllegalStateException("S'ha superat el nombre màxim d'intents (" + MAX_TITOL_TRIES + ") per generar un títol únic per al widget: " + originalTitol);
                }
                int maxLength = EstadisticaWidgetEntity.TITOL_MAX_LENGTH;
                String suffix = counter == 1 ? " (Copia)" : " (Copia " + counter + ")";
                String base = originalTitol != null && originalTitol.length() + suffix.length() > maxLength
                        ? originalTitol.substring(0, maxLength - suffix.length())
                        : (originalTitol != null ? originalTitol : "Widget");
                candidate = base + suffix;
                counter++;
            }
            return candidate;
        }

        @Override
        public void onChange(Serializable id, Dashboard previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Dashboard target) {
        }

    }

}
