package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltre;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardFiltreRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.estadistica.persist.repository.PlantillaRepository;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
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
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardHelper {

    private final EstadisticaClientHelper estadisticaClientHelper;
    private final ResourceEntityMappingHelper resourceEntityMappingHelper;

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
        if (Objects.nonNull(entity.getFiltres())) {
            afterConversionGetFiltres(entity, resource);
        }
    }

    /**
     * El mapeig genèric d'entitat a recurs (ObjectMappingHelper.map) no sap convertir camps de tipus
     * List&lt;Entity&gt; (com filtres, items o titols) i sempre els deixa a null; per això cal fer la conversió
     * manualment aquí, igual que ja es fa amb el nom de l'aplicació i de l'entorn. Sense això, el frontend mai
     * rep els filtres de capçalera configurats (vegeu DashboardFiltreBar) encara que existeixin a la BD.
     */
    private void afterConversionGetFiltres(DashboardEntity entity, Dashboard resource) {
        if (entity.getFiltres() != null) {
            resource.setFiltres(
                entity.getFiltres().stream()
                    .map(filtreEntity -> resourceEntityMappingHelper.entityToResource(filtreEntity, DashboardFiltre.class))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * Assigna el nom de l'aplicació a partir de l'appId
     **/
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

    /**
     * Assigna el nom de l'entorn a partir de l'entornId
     **/
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

    private static EntornApp getEntornAppOrDefaultNull(EstadisticaClientHelper client, Long appId, Long entornId) {
        if (appId == null || entornId == null) {
            return null;
        }
        try {
            return client.entornAppFindByAppAndEntorn(appId, entornId);
        } catch (es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            log.warn("Error getting EntornApp for appId=" + appId + " and entornId=" + entornId, ex);
            return null;
        }
    }

    public void beforeUpdateEntityLogic(DashboardEntity entity,
                                        Dashboard resource,
                                        Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        beforeUpdateChangeEntornApp(entity, resource, answers);
    }

    private void beforeUpdateChangeEntornApp(DashboardEntity entity,
                                             Dashboard resource,
                                             Map<String, AnswerRequiredException.AnswerValue> answers) {
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

        Long newAppId = canviAppId ? resource.getAppId() : entity.getAppId();
        Long newEntornId = canviEntornId ? resource.getEntornId() : entity.getEntornId();

        EntornApp newEntornApp = (Objects.nonNull(newAppId) && Objects.nonNull(newEntornId)) ?
            getEntornAppOrDefaultNull(estadisticaClientHelper, newAppId, newEntornId) : null;

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

            // Comprovam que existeixi entornApp de destí
            if (Objects.nonNull(newEntornApp)) {
                item.setEntornId(newEntornId);
            } else {
                throw new AnswerRequiredException(
                    Dashboard.class,
                    ANSWER_CODE_ENTORN_ID,
                    I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.entornId"),
                    null
                );
            }
        }
    }

    public static class CloneDashboardAction implements BaseMutableResourceService.ActionExecutor<DashboardEntity, Dashboard, Dashboard> {

        private final EstadisticaClientHelper estadisticaClientHelper;
        private final DashboardRepository dashboardRepository;
        private final DashboardTitolRepository dashboardTitolRepository;
        private final DashboardItemRepository dashboardItemRepository;
        private final DashboardFiltreRepository dashboardFiltreRepository;
        private final PlantillaRepository plantillaRepository;
        private final EstadisticaWidgetRepository estadisticaWidgetRepository;
        private final es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper;
        private final AtributsVisualsHelper atributsVisualsHelper;

        public CloneDashboardAction(EstadisticaClientHelper estadisticaClientHelper,
                                    DashboardRepository dashboardRepository,
                                    DashboardTitolRepository dashboardTitolRepository,
                                    DashboardItemRepository dashboardItemRepository,
                                    DashboardFiltreRepository dashboardFiltreRepository,
                                    PlantillaRepository plantillaRepository,
                                    EstadisticaWidgetRepository estadisticaWidgetRepository,
                                    es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper) {
            this(estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, dashboardFiltreRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper, null);
        }

        public CloneDashboardAction(EstadisticaClientHelper estadisticaClientHelper,
                                    DashboardRepository dashboardRepository,
                                    DashboardTitolRepository dashboardTitolRepository,
                                    DashboardItemRepository dashboardItemRepository,
                                    DashboardFiltreRepository dashboardFiltreRepository,
                                    PlantillaRepository plantillaRepository,
                                    EstadisticaWidgetRepository estadisticaWidgetRepository,
                                    es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper,
                                    AtributsVisualsHelper atributsVisualsHelper) {
            this.estadisticaClientHelper = estadisticaClientHelper;
            this.dashboardRepository = dashboardRepository;
            this.dashboardTitolRepository = dashboardTitolRepository;
            this.dashboardItemRepository = dashboardItemRepository;
            this.dashboardFiltreRepository = dashboardFiltreRepository;
            this.plantillaRepository = plantillaRepository;
            this.estadisticaWidgetRepository = estadisticaWidgetRepository;
            this.dashboardClonerMapper = dashboardClonerMapper;
            this.atributsVisualsHelper = atributsVisualsHelper;
        }

        public CloneDashboardAction(EstadisticaClientHelper estadisticaClientHelper,
                                    DashboardRepository dashboardRepository,
                                    DashboardTitolRepository dashboardTitolRepository,
                                    DashboardItemRepository dashboardItemRepository,
                                    PlantillaRepository plantillaRepository,
                                    EstadisticaWidgetRepository estadisticaWidgetRepository,
                                    es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper) {
            this(estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, null, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper, null);
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
            // El color de fons (clar/fosc) no és un paràmetre editable en clonar: sempre s'ha d'heretar de
            // l'entitat original, independentment que s'hagin proporcionat altres paràmetres.
            newDashboard.setColorFonsClar(entity.getColorFonsClar());
            newDashboard.setColorFonsFosc(entity.getColorFonsFosc());
            List<DashboardTitolEntity> clonedTitols = getClonedTitulos(entity, newDashboard);
            List<DashboardItemEntity> clonedItems = getClonedItem(entity, newDashboard);
            List<DashboardFiltreEntity> clonedFiltres = getClonedFiltres(entity, newDashboard);
            dashboardRepository.save(newDashboard);
            dashboardTitolRepository.saveAll(clonedTitols);
            dashboardItemRepository.saveAll(clonedItems);
            if (dashboardFiltreRepository != null && !clonedFiltres.isEmpty()) {
                dashboardFiltreRepository.saveAll(clonedFiltres);
            }
            return null;
        }

        private List<DashboardFiltreEntity> getClonedFiltres(DashboardEntity originalDashboard,
                                                             DashboardEntity newDashboard) {
            List<DashboardFiltreEntity> clonedFiltres = new ArrayList<>();
            if (originalDashboard.getFiltres() != null) {
                for (DashboardFiltreEntity original : originalDashboard.getFiltres()) {
                    DashboardFiltreEntity clone = dashboardClonerMapper.cloneFiltre(original);
                    clone.setDashboard(newDashboard);
                    clonedFiltres.add(clone);
                }
            }
            newDashboard.setFiltres(clonedFiltres);
            return clonedFiltres;
        }

        private List<DashboardTitolEntity> getClonedTitulos(DashboardEntity originalDashboard,
                                                            DashboardEntity newDashboard) {
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

        private List<DashboardItemEntity> getClonedItem(DashboardEntity originalDashboard,
                                                        DashboardEntity newDashboard) {
            List<DashboardItemEntity> clonedItems = new ArrayList<>();
            Map<Long, EstadisticaWidgetEntity> clonedWidgetsMap = new HashMap<>();
            if (originalDashboard.getItems() != null) {
                boolean canviAppId = !Objects.equals(originalDashboard.getAppId(), newDashboard.getAppId()) && Objects.nonNull(newDashboard.getAppId());
                boolean canviEntornId = !Objects.equals(originalDashboard.getEntornId(), newDashboard.getEntornId()) && Objects.nonNull(newDashboard.getEntornId());
                Long newAppId = newDashboard.getAppId();
                Long newEntornId = newDashboard.getEntornId();
                EntornApp newEntornApp = (Objects.nonNull(newAppId) && Objects.nonNull(newEntornId)) ?
                    getEntornAppOrDefaultNull(estadisticaClientHelper, newAppId, newEntornId) : null;
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
                        if (clone.getPersonalitzat() == null || !clone.getPersonalitzat()) {
                            if (atributsVisualsHelper != null && atributsVisualsHelper.hasVisualOverrides(clonedWidget)) {
                                clone.setPersonalitzat(true);
                            }
                        }
                    }
                    Long resolvedEntornId;
                    if (!canviAppId && !canviEntornId) {
                        resolvedEntornId = original.getEntornId();
                    } else if (newEntornApp != null) {
                        resolvedEntornId = newEntornId != null ? newEntornId : original.getEntornId();
                    } else {
                        Long itemAppId = Objects.nonNull(newAppId) ? newAppId : (original.getWidget() != null ? original.getWidget().getAppId() : null);
                        Long itemEntornId = Objects.nonNull(newEntornId) ? newEntornId : original.getEntornId();
                        EntornApp newItemEntornApp = getEntornAppOrDefaultNull(estadisticaClientHelper, itemAppId, itemEntornId);
                        if (Objects.isNull(newItemEntornApp)) {
                            throw new ActionExecutionException(
                                Dashboard.class,
                                originalDashboard.getId(),
                                Dashboard.CLONE_ACTION,
                                I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.helper.DashboardHelper.action.error.appId")
                            );
                        }
                        resolvedEntornId = itemEntornId;
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
            return DashboardHelper.cloneWidgetLogic(original, targetAppId, clonedWidgetsMap, estadisticaWidgetRepository, dashboardClonerMapper);
        }

        @Override
        public void onChange(Serializable id,
                             Dashboard previous,
                             String fieldName,
                             Object fieldValue,
                             Map<String, AnswerRequiredException.AnswerValue> answers,
                             String[] previousFieldNames,
                             Dashboard target) {
        }
    }

    private static final int MAX_TITOL_TRIES = 1000;

    public static String getWidgetNewTitolLogic(String originalTitol, Long appId, EstadisticaWidgetRepository estadisticaWidgetRepository) {
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

    public static EstadisticaWidgetEntity cloneWidgetLogic(
            EstadisticaWidgetEntity original,
            Long targetAppId,
            Map<Long, EstadisticaWidgetEntity> clonedWidgetsMap,
            EstadisticaWidgetRepository estadisticaWidgetRepository,
            es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper) {
        if (original == null) {
            return null;
        }
        if (original.getId() != null && clonedWidgetsMap.containsKey(original.getId())) {
            return clonedWidgetsMap.get(original.getId());
        }
        Long appId = targetAppId != null ? targetAppId : original.getAppId();
        String newTitol = getWidgetNewTitolLogic(original.getTitol(), appId, estadisticaWidgetRepository);

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

    public static class CloneAndAddWidgetAction implements BaseMutableResourceService.ActionExecutor<DashboardEntity, es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.CloneAndAddWidgetParams, es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem> {

        private final EstadisticaClientHelper estadisticaClientHelper;
        private final DashboardItemRepository dashboardItemRepository;
        private final EstadisticaWidgetRepository estadisticaWidgetRepository;
        private final es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper;
        private final DashboardItemTitolHelper dashboardItemTitolHelper;
        private final AtributsVisualsHelper atributsVisualsHelper;

        public CloneAndAddWidgetAction(EstadisticaClientHelper estadisticaClientHelper,
                                       DashboardItemRepository dashboardItemRepository,
                                       EstadisticaWidgetRepository estadisticaWidgetRepository,
                                       es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper) {
            this(estadisticaClientHelper, dashboardItemRepository, estadisticaWidgetRepository, dashboardClonerMapper, null, null);
        }

        public CloneAndAddWidgetAction(EstadisticaClientHelper estadisticaClientHelper,
                                       DashboardItemRepository dashboardItemRepository,
                                       EstadisticaWidgetRepository estadisticaWidgetRepository,
                                       es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper,
                                       DashboardItemTitolHelper dashboardItemTitolHelper) {
            this(estadisticaClientHelper, dashboardItemRepository, estadisticaWidgetRepository, dashboardClonerMapper, dashboardItemTitolHelper, null);
        }

        public CloneAndAddWidgetAction(EstadisticaClientHelper estadisticaClientHelper,
                                       DashboardItemRepository dashboardItemRepository,
                                       EstadisticaWidgetRepository estadisticaWidgetRepository,
                                       es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper dashboardClonerMapper,
                                       DashboardItemTitolHelper dashboardItemTitolHelper,
                                       AtributsVisualsHelper atributsVisualsHelper) {
            this.estadisticaClientHelper = estadisticaClientHelper;
            this.dashboardItemRepository = dashboardItemRepository;
            this.estadisticaWidgetRepository = estadisticaWidgetRepository;
            this.dashboardClonerMapper = dashboardClonerMapper;
            this.dashboardItemTitolHelper = dashboardItemTitolHelper;
            this.atributsVisualsHelper = atributsVisualsHelper;
        }

        @Override
        public es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem exec(String code, DashboardEntity entity, es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.CloneAndAddWidgetParams params) throws ActionExecutionException {
            if (params == null || params.getWidgetId() == null) {
                throw new ActionExecutionException(Dashboard.class, entity.getId(), code, "widgetId is required");
            }

            EstadisticaWidgetEntity originalWidget = estadisticaWidgetRepository.findById(params.getWidgetId()).orElseThrow(() ->
                new ActionExecutionException(Dashboard.class, entity.getId(), code, "Original widget not found")
            );

            Map<Long, EstadisticaWidgetEntity> clonedWidgetsMap = new HashMap<>();
            EstadisticaWidgetEntity newWidget = DashboardHelper.cloneWidgetLogic(originalWidget, entity.getAppId(), clonedWidgetsMap, estadisticaWidgetRepository, dashboardClonerMapper);

            DashboardItemEntity newItem = new DashboardItemEntity();
            newItem.setDashboard(entity);
            newItem.setWidget(newWidget);

            Long entornId = params.getEntornId() != null ? params.getEntornId() : entity.getEntornId();
            if (entornId != null) {
                newItem.setEntornId(entornId);
            }

            int width = params.getWidth() != null ? params.getWidth() : 3;
            int height = params.getHeight() != null ? params.getHeight() : 3;
            newItem.setWidth(width);
            newItem.setHeight(height);

            int posX;
            int posY;
            if (params.getPosX() != null && params.getPosY() != null) {
                posX = params.getPosX();
                posY = params.getPosY();
            } else if (dashboardItemTitolHelper != null) {
                DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(entity.getId(), width, height);
                posX = params.getPosX() != null ? params.getPosX() : pos.getPosX();
                posY = params.getPosY() != null ? params.getPosY() : pos.getPosY();
            } else {
                posX = params.getPosX() != null ? params.getPosX() : 0;
                posY = params.getPosY() != null ? params.getPosY() : 0;
            }

            newItem.setPosX(posX);
            newItem.setPosY(posY);

            boolean hasOverrides = false;
            if (atributsVisualsHelper != null) {
                hasOverrides = atributsVisualsHelper.hasVisualOverrides(newWidget != null ? newWidget : originalWidget);
            } else if (originalWidget.getAtributsVisualsJson() != null && !originalWidget.getAtributsVisualsJson().trim().isEmpty() && !"{ }".equals(originalWidget.getAtributsVisualsJson().trim()) && !"{}".equals(originalWidget.getAtributsVisualsJson().trim())) {
                hasOverrides = true;
            }
            if (hasOverrides) {
                newItem.setPersonalitzat(true);
            }

            dashboardItemRepository.save(newItem);

            // Map the entity to the resource and return it
            es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem resource = new es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem();
            resource.setId(newItem.getId());
            resource.setPosX(newItem.getPosX());
            resource.setPosY(newItem.getPosY());
            resource.setWidth(newItem.getWidth());
            resource.setHeight(newItem.getHeight());
            resource.setDestacat(newItem.getDestacat());
            resource.setPersonalitzat(newItem.getPersonalitzat());
            if (newWidget != null) {
                resource.setWidget(ResourceReference.toResourceReference(newWidget.getId(), newWidget.getTitol()));
            }
            if (newItem.getEntornId() != null) {
                resource.setEntornId(newItem.getEntornId());
            }
            return resource;
        }

        @Override
        public void onChange(Serializable id, es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.CloneAndAddWidgetParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.CloneAndAddWidgetParams target) {
        }
    }

}
