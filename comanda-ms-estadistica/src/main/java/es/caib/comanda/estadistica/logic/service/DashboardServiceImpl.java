package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardStyleResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTitol;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTitolItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitolTipus;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.logic.intf.service.DashboardService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaTaulaWidgetService;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FileReference;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els dashboards.
 * Aquesta classe extend BaseReadonlyResourceService i implementa la interfície DashboardService.
 *
 * Proporciona funcionalitats específiques per treballar amb el model de dades Dashboard,
 * interactuant amb l'entitat persistent DashboardEntity.
 *
 * Aquesta classe utilitza anotacions de Spring per ser detectada com a servei,
 * i registra logs mitjançant Lombok.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardServiceImpl extends BaseMutableResourceService<Dashboard, Long, DashboardEntity> implements DashboardService {

    private final ConsultaEstadisticaHelper consultaEstadisticaHelper;
    private final EstadisticaSimpleWidgetService estadisticaSimpleWidgetService;
    private final EstadisticaGraficWidgetService estadisticaGraficWidgetService;
    private final EstadisticaTaulaWidgetService estadisticaTaulaWidgetService;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final AtributsVisualsHelper atributsVisualsHelper;
    private final DashboardExportMapper dashboardExportMapper;
    private final ObjectMapper objectMapper;
    private final DashboardHelper dashboardHelper;
    private final DashboardRepository dashboardRepository;
    private final DashboardTitolRepository dashboardTitolRepository;
    private final DashboardItemRepository dashboardItemRepository;
    private final DashboardStyleResolverHelper dashboardStyleResolverHelper;
    private final PlantillaRepository plantillaRepository;
    private final IndicadorRepository indicadorRepository;
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;

    @PostConstruct
    public void init() {
        register(Dashboard.WIDGETS_REPORT, new InformeWidgets());
        register(Dashboard.DASHBOARD_EXPORT, new DashboardExportReportGenerator());
        register(Dashboard.DASHBOARD_IMPORT, new DashboardImportActionExecutor());
        register(Dashboard.CLONE_ACTION, (ActionExecutor<DashboardEntity, ?, ?>) new DashboardHelper.CloneDashboardAction(estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository));
    }

    @Override
    protected void completeResource(Dashboard resource) {
        dashboardHelper.completeResourceLogic(resource);
    }

    @Override
    protected void beforeUpdateEntity(DashboardEntity entity, Dashboard resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        dashboardHelper.beforeUpdateEntityLogic(entity, resource, answers);
    }

    @Override
    protected void afterConversion(DashboardEntity entity, Dashboard resource) {
        dashboardHelper.afterConversionLogic(entity, resource);
    }

    private DashboardEntity getDashboard(String code, DashboardEntity entity) {
        DashboardEntity dashboard = entityRepository.findById(entity.getId())
                .orElseThrow(() -> new ReportGenerationException(Dashboard.class, entity.getId(), code, "No existeix"));
        return dashboard;
    }

    // REPORT PER OBTENIR EMPLENAR I WIDGETS
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class InformeWidgets implements ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> {

        @Override
        public List<InformeWidgetItem> generateData(
                String code,
                DashboardEntity entity,
                InformeWidgetParams params) throws ReportGenerationException {

            DashboardEntity dashboard = getDashboard(code, entity);
            List<InformeWidgetItem> dashboardItems = new ArrayList<>();
            List<InformeWidgetItem> dashboartTitols = new ArrayList<>();
            if (dashboard.getItems() != null) {
                dashboardItems = dashboard.getItems().stream()
                        .map(item -> {
                            InformeWidgetItem informeItem = InformeWidgetItem.builder()
                                    .dashboardItemId(item.getId())
                                    .widgetId(item.getWidget().getId())
                                    .titol(item.getWidget() != null ? item.getWidget().getTitol() : null)
                                    .tipus(consultaEstadisticaHelper.determineWidgetType(item))
                                    .posX(item.getPosX())
                                    .posY(item.getPosY())
                                    .width(item.getWidth())
                                    .height(item.getHeight())
                                    .destacat(Boolean.TRUE.equals(item.getDestacat()))
                                    .loading(true)
                                    .build();
                            return informeItem;
                        })
                        .collect(Collectors.toList());
                log.debug("Dashboard {}: {} items", entity.getId(), dashboardItems.size());
            }
            if (dashboard.getTitols() != null) {
                boolean temaFosc = params != null && Boolean.TRUE.equals(params.getTemaFosc());
                dashboartTitols = dashboard.getTitols().stream()
                        .map(titol -> {
                            AtributsVisualsTitol atributsVisuals = resolveAtributsVisualsTitol(titol, dashboard, temaFosc);
                            InformeWidgetTitolItem informeTitol = InformeWidgetTitolItem.builder()
                                    .dashboardTitolId(titol.getId())
                                    .tipus(WidgetTipus.TITOL)
                                    .titol(titol.getTitol())
                                    .subtitol(titol.getSubtitol())
                                    .posX(titol.getPosX())
                                    .posY(titol.getPosY())
                                    .width(titol.getWidth())
                                    .height(titol.getHeight())
                                    .destacat(Boolean.TRUE.equals(titol.getDestacat()))
                                    .atributsVisuals(atributsVisuals)
                                    .build();
                            return informeTitol;
                        })
                        .collect(Collectors.toList());
                log.debug("Dashboard {}: {} titols", entity.getId(), dashboartTitols.size());
            }
            dashboardItems.addAll(dashboartTitols);
            return dashboardItems;
        }

        /** Resol els atributs visuals d'un títol aplicant primer els valors propis i després la plantilla. **/
        private AtributsVisualsTitol resolveAtributsVisualsTitol(
                DashboardTitolEntity titol,
                DashboardEntity dashboard,
                boolean temaFosc) {
            AtributsVisualsTitol atributsVisuals = AtributsVisualsTitol.builder()
                    .colorTitol(titol.getColorTitol())
                    .midaFontTitol(titol.getMidaFontTitol())
                    .colorSubtitol(titol.getColorSubtitol())
                    .midaFontSubtitol(titol.getMidaFontSubtitol())
                    .colorFons(titol.getColorFons())
                    .mostrarVora(titol.getMostrarVora())
                    .colorVora(titol.getColorVora())
                    .ampleVora(titol.getAmpleVora())
                    .build();
            PlantillaEntity plantilla = titol.getPlantilla() != null
                    ? titol.getPlantilla()
                    : (dashboard != null ? dashboard.getPlantilla() : null);
            if (plantilla != null) {
                boolean destacat = Boolean.TRUE.equals(titol.getDestacat());
                PaletteGroupType groupType = temaFosc
                        ? (destacat ? PaletteGroupType.DARK_HIGHLIGHTED : PaletteGroupType.DARK)
                        : (destacat ? PaletteGroupType.LIGHT_HIGHLIGHTED : PaletteGroupType.LIGHT);
                dashboardStyleResolverHelper.applyTemplateDefaults(atributsVisuals, plantilla, groupType, titleStyleScope(titol.getTipusTitol()));
            }
            return atributsVisuals;
        }

        private WidgetStyleScope titleStyleScope(DashboardTitolTipus tipusTitol) {
            if (DashboardTitolTipus.TIPUS_2.equals(tipusTitol)) {
                return WidgetStyleScope.TITOL_2;
            }
            if (DashboardTitolTipus.TIPUS_3.equals(tipusTitol)) {
                return WidgetStyleScope.TITOL_3;
            }
            return WidgetStyleScope.TITOL_1;
        }

        @Override
        public void onChange(Serializable id, InformeWidgetParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, InformeWidgetParams target) {
        }
    }

    /**
     * Generador d'informes per exportar dashboards en format JSON.
     */
    public class DashboardExportReportGenerator implements ReportGenerator<DashboardEntity, Serializable, DashboardExport> {
        @Override
        public List<DashboardExport> generateData(String code, DashboardEntity entity, Serializable params) throws ReportGenerationException {
            List<DashboardExport> result = new ArrayList<>();

            // Si s'ha especificat una entitat, només exportem aquesta
            if (entity != null) {
                DashboardExport dashboard = dashboardExportMapper.toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper);
                result.add(dashboard);
            } else {
                // Si no s'ha especificat una entitat, exportem tots els dashboards
                List<DashboardEntity> entities = entityRepository.findAll();
                List<DashboardExport> dashboards = dashboardExportMapper.toDashboardExport(entities, estadisticaClientHelper, atributsVisualsHelper);
                result.addAll(dashboards);
            }

            return result;
        }

        @Override
        public DownloadableFile generateFile(String code, List<?> data, ReportFileType fileType, OutputStream out) {
            try {
                // Utilitzem un ByteArrayOutputStream per capturar el contingut
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(baos, data);

                // Escrivim el contingut a l'OutputStream original
                byte[] content = baos.toByteArray();
                out.write(content);

                return new DownloadableFile("dashboards.json", "application/json", content);
            } catch (IOException e) {
                log.error("Error generating JSON file", e);
                return null;
            }
        }

        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
            // No es necessari implementar aquest mètode
        }
    }

    /**
     * Paràmetres per a la importació de dashboards.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardImportParams implements Serializable {
        @NotNull private FileReference file;
        @NotNull private OverwriteEnum overwrite = OverwriteEnum.EMPRAR_EXISTENT;
    }

    /**
     * Classe que encapsula una llista de dashboards per a la importació/exportació.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardImportResult implements Serializable {
        private List<Dashboard> dashboards;
    }

    /**
     * ActionExecutor per a la importació de dashboards des d'un fitxer JSON.
     * Aquesta classe permet importar dashboards i els seus elements relacionats.
     */
    public class DashboardImportActionExecutor implements ActionExecutor<DashboardEntity, DashboardImportParams, DashboardImportResult> {
        private String getRecursNom(Long appId, String nomEntrada) {
            int contador = 0;
            String temp = nomEntrada;
            EstadisticaWidgetEntity existWidget = estadisticaWidgetRepository.findByAppIdAndTitol(appId, temp);
            while (existWidget != null) {
                contador++;
                temp = nomEntrada + " (" + contador + ")";
                existWidget = estadisticaWidgetRepository.findByAppIdAndTitol(appId, temp);
            }
            return temp;
        }

        @Override
        public DashboardImportResult exec(String code, DashboardEntity entity, DashboardImportParams params) {
            try {
                String jsonString = new String(params.getFile().getContent(), StandardCharsets.UTF_8);

                List<DashboardExport> dashboards = objectMapper.readValue(jsonString,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, DashboardExport.class));

                List<DashboardEntity> dashboardsToImport = dashboardExportMapper.toDashboardEntity(
                        dashboards,
                        estadisticaClientHelper,
                        atributsVisualsHelper,
                        plantillaRepository,
                        indicadorRepository,
                        dimensioRepository,
                        dimensioValorRepository);

                List<Dashboard> importedDashboards = new ArrayList<>();
                for (DashboardEntity dashboardEntity: dashboardsToImport) {
                    dashboardRepository.saveAndFlush(dashboardEntity);
                    dashboardEntity.getTitols().forEach(t->t.setDashboard(dashboardEntity));
                    dashboardTitolRepository.saveAllAndFlush(dashboardEntity.getTitols());
                    dashboardEntity.getItems().forEach(t -> {
                        EstadisticaWidgetEntity existWidget = estadisticaWidgetRepository.findByAppIdAndTitol(t.getWidget().getAppId(), t.getWidget().getTitol());

                        if (existWidget != null) {
                            switch (params.getOverwrite()) {
                                case EMPRAR_EXISTENT:
                                    t.setWidget(existWidget);
                                    break;
                                case CREAR_AMB_ALTRE_NOM:
                                    t.getWidget().setTitol(
                                            getRecursNom(
                                                    t.getWidget().getAppId(),
                                                    t.getWidget().getTitol()
                                            )
                                    );
                                    break;
                            }
                        }

                        EstadisticaWidgetEntity widget = t.getWidget();
                        if (widget instanceof EstadisticaSimpleWidgetEntity) {
                            ((EstadisticaSimpleWidgetEntity) widget).getIndicadorInfo().setWidget(widget);
                        }
                        if (widget instanceof EstadisticaGraficWidgetEntity) {
                            ((EstadisticaGraficWidgetEntity) widget).getIndicadorsInfo().forEach(c -> {
                                c.setWidget(widget);
                            });
                        }
                        if (widget instanceof EstadisticaTaulaWidgetEntity) {
                            ((EstadisticaTaulaWidgetEntity) widget).getColumnes().forEach(c -> {
                                c.setWidget(widget);
                            });
                        }

                        estadisticaWidgetRepository.saveAndFlush(widget);
                        t.setDashboard(dashboardEntity);
                    });
                    dashboardItemRepository.saveAllAndFlush(dashboardEntity.getItems());
                }

                return new DashboardImportResult(importedDashboards);
            } catch (Exception e) {
                log.error("Error importing dashboards from JSON", e);
                throw new ActionExecutionException(
                        Dashboard.class,
                        null,
                        code,
                        e.getMessage());
            }
        }

        /**
         * Verifica si els widgets referenciats en un dashboard existeixen.
         *
         * Nota: Aquesta implementació només verifica si els widgets existeixen, però no els crea si no existeixen.
         * Per crear widgets durant la importació, necessitaríem tenir la informació completa del widget,
         * no només una referència. Això requeriria modificar l'exportació per incloure la informació completa
         * dels widgets, i modificar la importació per crear els widgets si no existeixen.
         *
         * @param dashboard El dashboard a verificar
         */
        private void checkWidgetsExistence(Dashboard dashboard) {
            if (dashboard.getItems() != null) {
                for (DashboardItem item : dashboard.getItems()) {
                    if (item.getWidget() != null) {
                        ResourceReference<EstadisticaSimpleWidget, Long> widgetRef = item.getWidget();
                        Long widgetId = widgetRef.getId();

                        if (widgetId != null) {
                            try {
                                // Intentem obtenir el widget per comprovar si existeix
                                estadisticaSimpleWidgetService.getOne(widgetId, null);
                                log.debug("Widget with id {} exists", widgetId);
                            } catch (Exception e) {
                                try {
                                    // Provem amb altres tipus de widgets
                                    estadisticaGraficWidgetService.getOne(widgetId, null);
                                    log.debug("GraficWidget with id {} exists", widgetId);
                                } catch (Exception e2) {
                                    try {
                                        estadisticaTaulaWidgetService.getOne(widgetId, null);
                                        log.debug("TaulaWidget with id {} exists", widgetId);
                                    } catch (Exception e3) {
                                        // Si no podem trobar el widget, registrem un avís
                                        log.warn("Widget with id {} does not exist. It will need to be created manually.", widgetId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onChange(Serializable id, DashboardImportParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, DashboardImportParams target) {
            // No es necessari implementar aquest mètode
        }
    }
}
