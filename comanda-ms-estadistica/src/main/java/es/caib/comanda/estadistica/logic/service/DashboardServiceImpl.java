package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.helper.*;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTitol;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTitolItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitolTipus;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.logic.intf.service.DashboardService;
import es.caib.comanda.estadistica.logic.intf.validation.ValidConflict;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.exception.*;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FileReference;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
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
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final AtributsVisualsHelper atributsVisualsHelper;
    private final DashboardExportMapper dashboardExportMapper;
    private final ObjectMapper objectMapper;
    private final DashboardHelper dashboardHelper;
    private final DashboardRepository dashboardRepository;
    private final DashboardTitolRepository dashboardTitolRepository;
    private final DashboardItemRepository dashboardItemRepository;
    private final PlantillaRepository plantillaRepository;
    private final DashboardStyleResolverHelper dashboardStyleResolverHelper;
    private final DashboardImportHelper dashboardImportHelper;
    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;

    @PostConstruct
    public void init() {
        register(Dashboard.PERSP_PERMIS_NUM, new PermisPerspective());
        register(Dashboard.WIDGETS_REPORT, new InformeWidgets());
        register(Dashboard.DASHBOARD_EXPORT, new DashboardExportReportGenerator());
        register(Dashboard.DASHBOARD_IMPORT, new DashboardImportActionExecutor());
        register(Dashboard.CLONE_ACTION, (ActionExecutor<DashboardEntity, ?, ?>) new DashboardHelper.CloneDashboardAction(estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository));
    }

    @Override
    protected String additionalSpringFilter(
        String currentSpringFilter,
        String[] namedQueries) {
        if (authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
            || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)) {
            return currentSpringFilter;
        }
        List<String> namedQueriesList = namedQueries!= null ? List.of(namedQueries) :Collections.emptyList();
        Set<Serializable> appPermissionIds = getAllowedIds(ResourceType.APP,
            namedQueriesList.contains("WRITE") ?List.of(PermissionEnum.PERM1) :List.of(PermissionEnum.PERM0, PermissionEnum.PERM1));
        String appFilter = SpringFilterHelper.buildOrFilter("appId", appPermissionIds);

        Set<Serializable> entornAppPermissionIds = getAllowedIds(ResourceType.ENTORN_APP,
            namedQueriesList.contains("WRITE") ?List.of(PermissionEnum.PERM1) :List.of(PermissionEnum.PERM0, PermissionEnum.PERM1) );
        String entornAppFilter = SpringFilterHelper.buildOrFilter("entornId", entornAppPermissionIds);

        Set<Serializable> dashboardPermissionIds = getAllowedIds(ResourceType.DASHBOARD,
            namedQueriesList.contains("WRITE") ?List.of(PermissionEnum.WRITE) :List.of(PermissionEnum.READ, PermissionEnum.WRITE));
        String dashboardFilter = SpringFilterHelper.buildOrFilter("id", dashboardPermissionIds);

        String filter = SpringFilterHelper.or(
            appFilter,
            entornAppFilter,
            dashboardFilter
        );

        return SpringFilterHelper.and(
            currentSpringFilter,
            (filter.isBlank())
                ? "id:0"
                : filter
        );
    }

    public Set<Serializable> getAllowedIds(ResourceType resourceType, List<PermissionEnum> permissions) {
        return Optional.ofNullable(aclServiceClient.findIdsWithAnyPermission(
                resourceType,
                permissions,
                authenticationHelper.getCurrentUserName(),
                Arrays.asList(authenticationHelper.getCurrentUserRealmRoles()),
                httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
            .orElse(Collections.emptySet());
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
            // Els camps propis del títol només sobreescriuen la plantilla si l'usuari ha activat
            // "personalitzat" explícitament; en cas contrari (encara que hi hagi valors residuals
            // guardats) s'ha d'aplicar sempre la plantilla amb prioritat (i el seu tema destacat).
            AtributsVisualsTitol atributsVisuals = Boolean.TRUE.equals(titol.getPersonalitzat())
                    ? AtributsVisualsTitol.builder()
                            .colorTitol(titol.getColorTitol())
                            .midaFontTitol(titol.getMidaFontTitol())
                            .colorSubtitol(titol.getColorSubtitol())
                            .midaFontSubtitol(titol.getMidaFontSubtitol())
                            .colorFons(titol.getColorFons())
                            .mostrarVora(titol.getMostrarVora())
                            .colorVora(titol.getColorVora())
                            .ampleVora(titol.getAmpleVora())
                            .build()
                    : AtributsVisualsTitol.builder().build();
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

    public class PermisPerspective implements PerspectiveApplicator<DashboardEntity, Dashboard> {
        @Override
        public void applySingle(String code, DashboardEntity entity, Dashboard resource) throws PerspectiveApplicationException {
            resource.setNumPermisos(
                Optional.ofNullable(aclServiceClient
                        .countSidsWithPermission(ResourceType.DASHBOARD, entity.getId(),
                            httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
                    .orElse(0));
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

                // Genera nom del fitxer
                String exportFileName = "dashboards.json";
                try {
                    if (data != null && data.size() == 1 && data.get(0) instanceof DashboardExport) {
                        String titol = ((DashboardExport) data.get(0)).getTitol();
                        if (titol != null && !titol.trim().isEmpty()) {
                            exportFileName = sanitizeFilename(titol.trim()) + ".json";
                        }
                    }
                } catch (Exception ex) {
                    log.error("Error generant el nom del fitxer d'exportació de Dashboards", ex);
                }

                return new DownloadableFile(exportFileName, "application/json", content);
            } catch (IOException e) {
                log.error("Error generating JSON file", e);
                return null;
            }
        }

        private String sanitizeFilename(String filename) {
            if (filename == null) {
                return "dashboard";
            }
            // 1. Normalitzem caràcters amb accents / diacrítics (ex: 'é' -> 'e', 'ç' -> 'c')
            String normalized = java.text.Normalizer.normalize(filename, java.text.Normalizer.Form.NFD);
            String withoutDiacritics = normalized.replaceAll("\\p{M}", "");

            // 2. Reemplacem caràcters especials / no permesos en noms de fitxers o capçaleres HTTP per '_'
            //    Permetem lletres, dígits, espais, guions, subratllats, punts i parèntesis
            String sanitized = withoutDiacritics.replaceAll("[^a-zA-Z0-9 _().-]", "_");

            // 3. Col·lapsem múltiples subratllats i espais consecutius
            sanitized = sanitized.replaceAll("_{2,}", "_").replaceAll(" {2,}", " ");

            // 4. Eliminem espais o caràcters especials dels extrems
            sanitized = sanitized.replaceAll("^[._\\-\\s]+|[._\\-\\s]+$", "").trim();

            return sanitized.isEmpty() ? "dashboard" : sanitized;
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
    @FieldNameConstants
    public static class DashboardImportParams implements Serializable {
        @NotNull @ResourceField(onChangeActive = true)
        private FileReference file;
        @Valid
        private List<Conflict> conflicts;

        private String nouNom;
        private OverwriteEnum overwrite;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    @ValidConflict
    public static class Conflict implements Serializable {
        @NotNull private String titol;
        @NotNull private OverwriteEnum overwrite = OverwriteEnum.EMPRAR_EXISTENT;
        private String nouNom;
        @NotNull private String tipo;

        private Long appId;

        public Conflict(String titol, String tipo) {
            this.titol = titol;
            this.tipo = tipo;
        }

        public Conflict(String titol, Long appId, String tipo) {
            this.titol = titol;
            this.appId = appId;
            this.tipo = tipo;
        }
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
                String jsonString = new String(params.getFile().getContent(), StandardCharsets.UTF_8);

                List<DashboardExport> dashboards = objectMapper.readValue(jsonString,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, DashboardExport.class));

                List<Dashboard> importedDashboards = new ArrayList<>();
                List<Conflict> conflicts = params.getConflicts() != null ? params.getConflicts() : Collections.emptyList();
                dashboardImportHelper.importDashboardFromExport(dashboards, conflicts);
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

        @Override
        public void onChange(Serializable id, DashboardImportParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, DashboardImportParams target) {
            if (DashboardImportParams.Fields.file.equals(fieldName)) {
                FileReference file = (FileReference) fieldValue;
                if (file == null || file.getContent() == null) {
                    target.setConflicts(new ArrayList<>());
                    return;
                }
                try {
                    String jsonString = new String(file.getContent(), StandardCharsets.UTF_8);
                    List<DashboardExport> dashboards = objectMapper.readValue(jsonString,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, DashboardExport.class));

                    List<Conflict> dashboardConflicts = new ArrayList<>();
                    dashboardImportHelper.checkDashboardConflicts(dashboards, dashboardConflicts);
                    target.setConflicts(dashboardConflicts);
                } catch (AnswerRequiredException a) {
                    if (!answers.containsKey(a.getAnswerCode())) {
                        throw a;
                    }
//                    throw new RuntimeException(a.getQuestion());
                } catch (Exception e) {
                    log.warn("Error parsing JSON content in onChange", e);
                }
            }
        }
    }
}
