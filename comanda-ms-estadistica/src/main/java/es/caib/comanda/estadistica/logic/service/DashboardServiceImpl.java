package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTitol;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTitolItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.logic.intf.service.DashboardService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaTaulaWidgetService;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @PostConstruct
    public void init() {
        register(Dashboard.WIDGETS_REPORT, new InformeWidgets());
        register(Dashboard.DASHBOARD_EXPORT, new DashboardExportReportGenerator());
        register(Dashboard.DASHBOARD_IMPORT, new DashboardImportActionExecutor());
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
                                    .titol(item.getWidget() != null ? item.getWidget().getTitol() : null)
                                    .tipus(consultaEstadisticaHelper.determineWidgetType(item))
                                    .posX(item.getPosX())
                                    .posY(item.getPosY())
                                    .width(item.getWidth())
                                    .height(item.getHeight())
                                    .loading(true)
                                    .build();
                            return informeItem;
                        })
                        .collect(Collectors.toList());
                log.debug("Dashboard {}: {} items", entity.getId(), dashboardItems.size());
            }
            if (dashboard.getTitols() != null) {
                dashboartTitols = dashboard.getTitols().stream()
                        .map(titol -> {
                            InformeWidgetTitolItem informeTitol = InformeWidgetTitolItem.builder()
                                    .dashboardTitolId(titol.getId())
                                    .tipus(WidgetTipus.TITOL)
                                    .titol(titol.getTitol())
                                    .subtitol(titol.getSubtitol())
                                    .posX(titol.getPosX())
                                    .posY(titol.getPosY())
                                    .width(titol.getWidth())
                                    .height(titol.getHeight())
                                    .atributsVisuals(AtributsVisualsTitol.builder()
                                            .colorTitol(titol.getColorTitol())
                                            .midaFontTitol(titol.getMidaFontTitol())
                                            .colorSubtitol(titol.getColorSubtitol())
                                            .midaFontSubtitol(titol.getMidaFontSubtitol())
                                            .colorFons(titol.getColorFons())
                                            .mostrarVora(titol.getMostrarVora())
                                            .colorVora(titol.getColorVora())
                                            .ampleVora(titol.getAmpleVora())
                                            .build())
                                    .build();
                            return informeTitol;
                        })
                        .collect(Collectors.toList());
                log.debug("Dashboard {}: {} titols", entity.getId(), dashboartTitols.size());
            }
            dashboardItems.addAll(dashboartTitols);
            return dashboardItems;
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
        private String jsonContent;
        private boolean overwrite;
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
        @Override
        public DashboardImportResult exec(String code, DashboardEntity entity, DashboardImportParams params) {
            try {
                List<Dashboard> dashboardsToImport = objectMapper.readValue(params.getJsonContent(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Dashboard.class));
                
                List<Dashboard> importedDashboards = new ArrayList<>();
                Map<String, AnswerRequiredException.AnswerValue> emptyAnswers = new HashMap<>();
                
                for (Dashboard dashboardToImport : dashboardsToImport) {
                    // Verificar si els widgets existeixen
                    checkWidgetsExistence(dashboardToImport);
                    
                    // Comprovar si el dashboard ja existeix
                    DashboardEntity existingDashboard = ((DashboardRepository)entityRepository).findByTitol(dashboardToImport.getTitol());
                    
                    if (existingDashboard != null) {
                        if (params.isOverwrite()) {
                            // Actualitzar el dashboard existent
                            Dashboard existingDashboardResource = entityToResource(existingDashboard);
                            existingDashboardResource.setTitol(dashboardToImport.getTitol());
                            existingDashboardResource.setDescripcio(dashboardToImport.getDescripcio());
                            existingDashboardResource.setItems(dashboardToImport.getItems());
                            
                            // Guardar el dashboard actualitzat
                            update(existingDashboardResource.getId(), existingDashboardResource, emptyAnswers);
                            importedDashboards.add(existingDashboardResource);
                        }
                    } else {
                        // Crear un nou dashboard
                        Dashboard createdDashboard = create(dashboardToImport, emptyAnswers);
                        importedDashboards.add(createdDashboard);
                    }
                }
                
                return new DashboardImportResult(importedDashboards);
            } catch (Exception e) {
                log.error("Error importing dashboards from JSON", e);
                throw new RuntimeException("Error importing dashboards: " + e.getMessage(), e);
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
