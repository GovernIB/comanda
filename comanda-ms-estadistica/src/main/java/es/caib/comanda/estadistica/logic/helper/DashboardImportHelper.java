package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.*;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardImportHelper {

    private final EstadisticaClientHelper estadisticaClientHelper;
    private final AtributsVisualsHelper atributsVisualsHelper;
    private final DashboardExportMapper dashboardExportMapper;
    private final DashboardRepository dashboardRepository;
    private final DashboardTitolRepository dashboardTitolRepository;
    private final DashboardItemRepository dashboardItemRepository;
    private final PlantillaRepository plantillaRepository;
    private final IndicadorRepository indicadorRepository;
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final PaletaRepository paletaRepository;
    private final Validator validator;

    public void validateDashboardExport(List<DashboardExport> dashboards) {
        if (dashboards == null || dashboards.isEmpty()) {
            throw new IllegalArgumentException("El fitxer no conté cap tauler de control per importar.");
        }
        for (int i = 0; i < dashboards.size(); i++) {
            DashboardExport dashboard = dashboards.get(i);
            if (validator != null) {
                Set<ConstraintViolation<DashboardExport>> violations = validator.validate(dashboard);
                if (!violations.isEmpty()) {
                    String errorDetails = violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .sorted()
                            .collect(Collectors.joining(", "));
                    String prefix = dashboards.size() > 1
                            ? "Tauler " + (i + 1) + (dashboard.getTitol() != null ? " (" + dashboard.getTitol() + ")" : "") + ": "
                            : "";
                    throw new IllegalArgumentException(prefix + "Dades del tauler invàlides (" + errorDetails + ")");
                }
            }
        }
    }

    public List<DashboardEntity> toDashboardEntity(List<DashboardExport> dashboardExportList) {
        return dashboardExportMapper.toDashboardEntity(
                dashboardExportList,
                estadisticaClientHelper,
                atributsVisualsHelper,
                indicadorRepository,
                dimensioRepository,
                dimensioValorRepository);
    }

    public List<DashboardEntity> importDashboardFromExport(List<DashboardExport> dashboardExportList, @NotNull List<Conflict> conflicts) {
        List<DashboardEntity> dashboardsToImport = this.toDashboardEntity(dashboardExportList);
        return this.importDashboardFromEntity(dashboardsToImport, conflicts);
    }
    public List<DashboardEntity> importDashboardFromEntity(List<DashboardEntity> dashboardEntityList, @NotNull List<Conflict> conflicts) {
        return dashboardEntityList.stream()
                .map(d -> this.importDashboardFromEntity(d, conflicts))
                .collect(Collectors.toList());
    }

    public DashboardEntity importDashboardFromEntity(DashboardEntity dashboardEntity, @NotNull List<Conflict> conflicts) {
        Conflict conflicte = this.findConflictByNom(
                dashboardEntity.getTitol(),
                null,
                DashboardExport.class.getSimpleName(),
                conflicts
        );

        if (conflicte != null) {
            switch (conflicte.getOverwrite()) {
                case EMPRAR_EXISTENT:
                    return dashboardRepository.findByTitol(dashboardEntity.getTitol());
                case CREAR_AMB_ALTRE_NOM:
                    String nom;
                    if (conflicte.getNouNom() != null && !conflicte.getNouNom().isBlank()) {
                        nom = conflicte.getNouNom();
                    } else {
                        nom = this.getElementNewNom(
                                dashboardEntity.getTitol(),
                                null,
                                DashboardExport.class.getSimpleName()
                        );
                    }
                    dashboardEntity.setTitol(nom);
                    break;
            }
        }

        if (dashboardEntity.getPlantilla() != null)
            dashboardEntity.setPlantilla(this.importPlantilla(dashboardEntity.getPlantilla(), conflicts));
        dashboardRepository.save(dashboardEntity);

        if (dashboardEntity.getTitols() != null) {
            this.importDashboardTitol(dashboardEntity.getTitols(), dashboardEntity, conflicts);
        }
        if (dashboardEntity.getItems() != null) {
            this.importDashboardItem(dashboardEntity.getItems(), dashboardEntity, conflicts);
        }
        return dashboardEntity;
    }

    private List<DashboardTitolEntity> importDashboardTitol(List<DashboardTitolEntity> dashboardItemEntityList, DashboardEntity dashboardEntity, List<Conflict> conflicts) {
        if (dashboardItemEntityList == null) return Collections.emptyList();
        return dashboardItemEntityList.stream()
                .map(d -> this.importDashboardTitol(d, dashboardEntity, conflicts))
                .collect(Collectors.toList());
    }

    private DashboardTitolEntity importDashboardTitol(DashboardTitolEntity dashboardTitolEntity, DashboardEntity dashboardEntity, List<Conflict> conflicts) {
        if (dashboardTitolEntity == null) return null;
        dashboardTitolEntity.setDashboard(dashboardEntity);
        if (dashboardTitolEntity.getPlantilla() != null)
            dashboardTitolEntity.setPlantilla(this.importPlantilla(dashboardTitolEntity.getPlantilla(), conflicts));
        dashboardTitolRepository.save(dashboardTitolEntity);
        return dashboardTitolEntity;
    }

    private List<DashboardItemEntity> importDashboardItem(List<DashboardItemEntity> dashboardItemEntityList, DashboardEntity dashboardEntity, List<Conflict> conflicts) {
        if (dashboardItemEntityList == null) return Collections.emptyList();
        return dashboardItemEntityList.stream()
                .map(d -> this.importDashboardItem(d, dashboardEntity, conflicts))
                .collect(Collectors.toList());
    }

    private DashboardItemEntity importDashboardItem(DashboardItemEntity dashboardItemEntity, DashboardEntity dashboardEntity, List<Conflict> conflicts) {
        if (dashboardItemEntity == null) return null;
        dashboardItemEntity.setDashboard(dashboardEntity);
        if (dashboardItemEntity.getWidget() != null) {
            dashboardItemEntity.setWidget(this.importWidget(dashboardItemEntity.getWidget(), conflicts));
        }
        if (dashboardItemEntity.getPlantilla() != null)
            dashboardItemEntity.setPlantilla(this.importPlantilla(dashboardItemEntity.getPlantilla(), conflicts));
        dashboardItemRepository.save(dashboardItemEntity);
        return dashboardItemEntity;
    }

    private EstadisticaWidgetEntity importWidget(EstadisticaWidgetEntity widgetEntity, List<Conflict> conflicts) {
        Conflict conflicte = this.findConflictByNom(
                widgetEntity.getTitol(),
                widgetEntity.getAppId(),
                EstadisticaWidgetExport.class.getSimpleName(),
                conflicts
        );

        if (conflicte != null) {
            switch (conflicte.getOverwrite()) {
                case EMPRAR_EXISTENT:
                    return estadisticaWidgetRepository.findByAppIdAndTitol(widgetEntity.getAppId(), widgetEntity.getTitol());
                case CREAR_AMB_ALTRE_NOM:
                    String nom;
                    if (conflicte.getNouNom() != null && !conflicte.getNouNom().isBlank()) {
                        nom = conflicte.getNouNom();

                        EstadisticaWidgetEntity widget = estadisticaWidgetRepository.findByAppIdAndTitol(widgetEntity.getAppId(), widgetEntity.getTitol());
                        if (widget != null) return widget;
                    } else {
                        nom = this.getElementNewNom(
                                widgetEntity.getTitol(),
                                widgetEntity.getAppId(),
                                EstadisticaWidgetExport.class.getSimpleName()
                        );
                    }
                    conflicte.setNouNom(nom);
                    widgetEntity.setTitol(nom);
                    break;
            }
        }

        if (widgetEntity instanceof EstadisticaSimpleWidgetEntity) {
            ((EstadisticaSimpleWidgetEntity) widgetEntity).getIndicadorInfo().setWidget(widgetEntity);
        }
        if (widgetEntity instanceof EstadisticaGraficWidgetEntity) {
            ((EstadisticaGraficWidgetEntity) widgetEntity).getIndicadorsInfo().forEach(c -> {
                c.setWidget(widgetEntity);
            });
        }
        if (widgetEntity instanceof EstadisticaTaulaWidgetEntity) {
            ((EstadisticaTaulaWidgetEntity) widgetEntity).getColumnes().forEach(c -> {
                c.setWidget(widgetEntity);
            });
        }

        estadisticaWidgetRepository.save(widgetEntity);
        return widgetEntity;
    }

    private PlantillaEntity importPlantilla(PlantillaEntity plantillaEntity, List<Conflict> conflicts) {
        Conflict conflicte = this.findConflictByNom(
                plantillaEntity.getNom(),
                null,
                PlantillaExport.class.getSimpleName(),
                conflicts
        );

        if (conflicte != null) {
            switch (conflicte.getOverwrite()) {
                case EMPRAR_EXISTENT:
                    return plantillaRepository.findByNom(plantillaEntity.getNom()).get();
                case CREAR_AMB_ALTRE_NOM:
                    String nom;
                    if (conflicte.getNouNom() != null && !conflicte.getNouNom().isBlank()) {
                        nom = conflicte.getNouNom();

                        PlantillaEntity plantilla = plantillaRepository.findByNom(nom).orElse(null);
                        if (plantilla != null) return plantilla;
                    } else {
                        nom = this.getElementNewNom(
                                plantillaEntity.getNom(),
                                null,
                                PlantillaExport.class.getSimpleName()
                        );
                    }
                    conflicte.setNouNom(nom);
                    plantillaEntity.setNom(nom);
                    break;
            }
        }

        this.importPlantillaGrupPaletes(plantillaEntity.getPaletteGroups(), plantillaEntity, conflicts);
        this.importWidgetStyleProperty(plantillaEntity.getStyleProperties(), plantillaEntity, conflicts);

        plantillaRepository.save(plantillaEntity);

        return plantillaEntity;
    }

    private List<WidgetStylePropertyEntity> importWidgetStyleProperty(List<WidgetStylePropertyEntity> widgetStylePropertyEntityList, PlantillaEntity plantillaEntity, List<Conflict> conflicts) {
        return widgetStylePropertyEntityList.stream()
                .map(d -> this.importWidgetStyleProperty(d, plantillaEntity, conflicts))
                .collect(Collectors.toList());
    }

    private WidgetStylePropertyEntity importWidgetStyleProperty(WidgetStylePropertyEntity widgetStylePropertyEntity, PlantillaEntity plantillaEntity, List<Conflict> conflicts) {
        widgetStylePropertyEntity.setPlantilla(plantillaEntity);
//        widgetStylePropertyRepository.save(widgetStylePropertyEntity);
        return widgetStylePropertyEntity;
    }

    private List<PlantillaGrupPaletesEntity> importPlantillaGrupPaletes(List<PlantillaGrupPaletesEntity> plantillaGrupPaletesEntityList, PlantillaEntity plantillaEntity, List<Conflict> conflicts) {
        return plantillaGrupPaletesEntityList.stream()
                .map(d -> this.importPlantillaGrupPaletes(d, plantillaEntity, conflicts))
                .collect(Collectors.toList());
    }

    private PlantillaGrupPaletesEntity importPlantillaGrupPaletes(PlantillaGrupPaletesEntity plantillaGrupPaletesEntity, PlantillaEntity plantillaEntity, List<Conflict> conflicts) {
        plantillaGrupPaletesEntity.setPlantilla(plantillaEntity);
        plantillaGrupPaletesEntity.setWidgetPalette(this.importPaleta(plantillaGrupPaletesEntity.getWidgetPalette(), conflicts));
        plantillaGrupPaletesEntity.setChartPalette(this.importPaleta(plantillaGrupPaletesEntity.getChartPalette(), conflicts));
//        dashboardTemplatePaletteGroupRepository.save(plantillaGrupPaletesEntity);
        return plantillaGrupPaletesEntity;
    }

    private PaletaEntity importPaleta(PaletaEntity paletaEntity, List<Conflict> conflicts) {
        Conflict conflicte = this.findConflictByNom(
                paletaEntity.getNom(),
                null,
                PaletaExport.class.getSimpleName(),
                conflicts
        );

        if (conflicte != null) {
            switch (conflicte.getOverwrite()) {
                case EMPRAR_EXISTENT:
                    return paletaRepository.findByNom(paletaEntity.getNom()).get();
                case CREAR_AMB_ALTRE_NOM:
                    String nom;
                    if (conflicte.getNouNom() != null && !conflicte.getNouNom().isBlank()) {
                        nom = conflicte.getNouNom();

                        PaletaEntity paleta = paletaRepository.findByNom(nom).orElse(null);
                        if (paleta != null) return paleta;
                    } else {
                        nom = this.getElementNewNom(
                                paletaEntity.getNom(),
                                null,
                                PaletaExport.class.getSimpleName()
                        );
                    }
                    conflicte.setNouNom(nom);
                    paletaEntity.setNom(nom);
                    break;
            }
        }
//        PaletaColorEntity
        this.importPaletaColor(paletaEntity.getColors(), paletaEntity);

        paletaRepository.save(paletaEntity);

        return paletaEntity;
    }

    private List<PaletaColorEntity> importPaletaColor(List<PaletaColorEntity> paletaColorEntityList, PaletaEntity paletaEntity) {
        return paletaColorEntityList.stream()
                .map(d -> this.importPaletaColor(d, paletaEntity))
                .collect(Collectors.toList());
    }

    private PaletaColorEntity importPaletaColor(PaletaColorEntity paletaColorEntity, PaletaEntity paletaEntity) {
        paletaColorEntity.setPaleta(paletaEntity);
//        paletaColorRepository.save(paletaColorEntity);
        return paletaColorEntity;
    }

    private Conflict findConflictByNom(String nom, Long appId, String tipus, List<Conflict> conflicts) {
        return conflicts.stream()
                .filter(c -> Objects.equals(nom, c.getTitol()) && Objects.equals(appId, c.getAppId()) && Objects.equals(tipus, c.getTipo()))
                .findFirst()
                .orElse(null);
    }

    /*///////////////////////////////////////////////////////////////////////*/

    public void checkDashboardConflicts(List<DashboardExport> dashboards,
                                        @NotNull List<Conflict> conflicts) {
        dashboards.forEach(d -> this.checkDashboardConflicts(d, conflicts));
    }

    public void checkDashboardConflicts(DashboardExport dashboard,
                                        @NotNull List<Conflict> conflicts) {
        if (dashboard == null) return;
        this.addConflict(dashboard.getTitol(), null, DashboardExport.class.getSimpleName(), conflicts);

        if (dashboard.getEntornCodi() != null) {
            this.checkEntorn(dashboard.getEntornCodi());
        }
        if (dashboard.getAppCodi() != null) {
            this.checkApp(dashboard.getAppCodi());
        }

        if (dashboard.getItems() != null) {
            for (DashboardItemExport item : dashboard.getItems()) {
                this.checkDashboardItemConflicts(item, conflicts);
            }
        }

        if (dashboard.getTitols() != null) {
            for (DashboardTitolExport titolExport : dashboard.getTitols()) {
                if (titolExport != null) {
                    this.checkPlantillaConflicts(titolExport.getPlantilla(), conflicts);
                }
            }
        }
        this.checkPlantillaConflicts(dashboard.getPlantilla(), conflicts);
    }

    private void checkDashboardItemConflicts(DashboardItemExport item,
                                             List<Conflict> conflicts) {
        if (item == null) return;
        App app = item.getAppCodi() != null ? this.checkApp(item.getAppCodi()) : null;
        Long appId = app != null ? app.getId() : null;
        if (item.getWidget() != null) {
            this.addConflict(item.getWidget().getTitol(), appId, EstadisticaWidgetExport.class.getSimpleName(), conflicts);
        }
        this.checkPlantillaConflicts(item.getPlantilla(), conflicts);

        EstadisticaWidgetExport widget = item.getWidget();
        if (widget != null && widget.getDimensionsValor() != null) {
            for (DimensioValorExport dmv : widget.getDimensionsValor()) {
                if (dmv != null && dmv.getDimensioCodi() != null) {
                    this.checkDimensio(dmv.getDimensioCodi(), item.getEntornCodi(), item.getAppCodi());
                }
            }
        }

        if (widget instanceof EstadisticaSimpleWidgetExport) {
            EstadisticaSimpleWidgetExport w = (EstadisticaSimpleWidgetExport) widget;
            if (w.getIndicadorInfo() != null)
                this.checkIndicador(w.getIndicadorInfo().getIndicadorCodi(), item.getEntornCodi(), item.getAppCodi());
        } else if (widget instanceof EstadisticaGraficWidgetExport) {
            EstadisticaGraficWidgetExport w = (EstadisticaGraficWidgetExport) widget;
            if (w.getIndicadorInfo() != null)
                this.checkIndicador(w.getIndicadorInfo().getIndicadorCodi(), item.getEntornCodi(), item.getAppCodi());
            if (w.getDescomposicioDimensioCodi() != null)
                this.checkDimensio(w.getDescomposicioDimensioCodi(), item.getEntornCodi(), item.getAppCodi());
        } else if (widget instanceof EstadisticaTaulaWidgetExport) {
            EstadisticaTaulaWidgetExport w = (EstadisticaTaulaWidgetExport) widget;
            if (w.getDimensioAgrupacioCodi() != null)
                this.checkDimensio(w.getDimensioAgrupacioCodi(), item.getEntornCodi(), item.getAppCodi());
        }
    }

    private void checkPlantillaConflicts(PlantillaExport plantillaExport,
                                        List<Conflict> conflicts) {
        if (plantillaExport == null) return;

        this.addConflict(plantillaExport.getNom(), null, PlantillaExport.class.getSimpleName(), conflicts);
        if (plantillaExport.getPaletteGroups() != null) {
            for (PlantillaGrupPaletesExport group : plantillaExport.getPaletteGroups()) {
                if (group != null) {
                    this.checkPaletaConflicts(group.getWidgetPalette(), conflicts);
                    this.checkPaletaConflicts(group.getChartPalette(), conflicts);
                }
            }
        }
    }

    private void checkPaletaConflicts(PaletaExport paletaExport,
                                        List<Conflict> conflicts) {
        if (paletaExport == null) return;
        this.addConflict(paletaExport.getNom(), null, PaletaExport.class.getSimpleName(), conflicts);
    }

    private void addConflict(String nom, Long appId, String tipus, List<Conflict> conflicts) {
        if (this.existsElementByNom(nom, appId, tipus)) {
            if (conflicts.stream()
                    .noneMatch(c -> Objects.equals(nom, c.getTitol()) && Objects.equals(appId, c.getAppId()) && Objects.equals(tipus, c.getTipo()))) {
                String suggerenciaNouNom = this.getElementNewNom(nom, appId, tipus);
                Conflict conflict = new Conflict(nom, appId, tipus);
                conflict.setSuggerenciaNouNom(suggerenciaNouNom);
                conflicts.add(conflict);
            }
        }
    }
    private Object getElementByNom(String nom, Long appId, String tipus) {
        if (DashboardExport.class.getSimpleName().equals(tipus)) {
            return dashboardRepository.findByTitol(nom);
        } else if (EstadisticaWidgetExport.class.getSimpleName().equals(tipus)) {
            return estadisticaWidgetRepository.findByAppIdAndTitol(appId, nom);
        } else if (PlantillaExport.class.getSimpleName().equals(tipus)) {
            return plantillaRepository.findByNom(nom).orElse(null);
        } else if (PaletaExport.class.getSimpleName().equals(tipus)) {
            return paletaRepository.findByNom(nom).orElse(null);
        }
        return null;
    }
    public boolean existsElementByNom(String nom, Long appId, String tipus) {
        return this.getElementByNom(nom, appId, tipus) != null;
    }

    private static final int MAX_TRIES_NOU_NOM = 100;

    private String getElementNewNom(String nomEntrada, Long appId, String tipus) {
        int contador = 0;
        String temp = nomEntrada;
        while (this.existsElementByNom(temp, appId, tipus)) {
            contador++;
            if (contador > MAX_TRIES_NOU_NOM) {
                return nomEntrada;
            }
            temp = nomEntrada + " (" + contador + ")";
        }
        return temp;
    }

    /*///////////////////////////////////////////////////////////////////////*/

    private Entorn checkEntorn(String entornCodi) {
        Entorn entorn = estadisticaClientHelper.entornByCodi(entornCodi);
        if (entorn == null) {
            throw new AnswerRequiredException(
                    DashboardServiceImpl.DashboardImportParams.class,
                    "ENTORN",
                    I18nUtil.getInstance().getI18nMessage(
                            "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.entorn",
                            entornCodi));
        }
        return entorn;
    }

    private App checkApp(String appCodi) {
        App app = estadisticaClientHelper.appFindByCodi(appCodi);
        if (app == null) {
            throw new AnswerRequiredException(
                    DashboardServiceImpl.DashboardImportParams.class,
                    "APP",
                    I18nUtil.getInstance().getI18nMessage(
                            "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.app",
                            appCodi));
        }
        return app;
    }

    private EntornApp checkEntornApp(String entornCodi, String appCodi) {
        Entorn entorn = this.checkEntorn(entornCodi);
        App app = this.checkApp(appCodi);
        EntornApp entornApp = null;
        try {
            entornApp = estadisticaClientHelper.entornAppFindByAppAndEntorn(app.getId(), entorn.getId());
        } catch (Exception ignore) {}
        if (entornApp == null) {
            throw new AnswerRequiredException(
                    DashboardServiceImpl.DashboardImportParams.class,
                    "ENTORN_APP",
                    I18nUtil.getInstance().getI18nMessage(
                            "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.entornApp",
                            entornCodi,
                            appCodi));
        }
        return entornApp;
    }

    private IndicadorEntity checkIndicador(String indicadorCodi, String entornCodi, String appCodi) {
        EntornApp entornApp = this.checkEntornApp(entornCodi, appCodi);
        IndicadorEntity indicador = indicadorRepository.findByCodiAndEntornAppId(indicadorCodi, entornApp.getId()).orElse(null);
        if (indicador == null) {
            throw new AnswerRequiredException(
                    DashboardServiceImpl.DashboardImportParams.class,
                    "INDICADOR",
                    I18nUtil.getInstance().getI18nMessage(
                            "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.indicador",
                            indicadorCodi,
                            entornApp.getId()));
        }
        return indicador;
    }

    private DimensioEntity checkDimensio(String dimensioCodi, String entornCodi, String appCodi) {
        EntornApp entornApp = this.checkEntornApp(entornCodi, appCodi);
        DimensioEntity dimensio = dimensioRepository.findByCodiAndEntornAppId(dimensioCodi, entornApp.getId()).orElse(null);
        if (dimensio == null) {
            throw new AnswerRequiredException(
                    DashboardServiceImpl.DashboardImportParams.class,
                    "DIMENSIO",
                    I18nUtil.getInstance().getI18nMessage(
                            "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.dimensio",
                            dimensioCodi,
                            entornApp.getId()));
        }
        return dimensio;
    }
}
