package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorFormulaTermeRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper per incloure els indicadors utilitzats pels widgets d'un dashboard dins de la seva exportació
 * (vegeu {@link es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport#getIndicadors()}), i per
 * crear-los (només els de tipus FORMULA) en importar-lo a un entorn on encara no existeixin.
 *
 * Els indicadors SIMPLE mai es creen des d'aquí: es gestionen només per sincronització automàtica des de les
 * apps (vegeu EstadisticaHelper i el comentari a Indicador). Si un SIMPLE no existeix a l'entorn destí, la
 * importació queda bloquejada abans d'arribar aquí (vegeu DashboardImportHelper#checkIndicador).
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndicadorExportHelper {

    private final EstadisticaClientHelper estadisticaClientHelper;
    private final DashboardExportMapper dashboardExportMapper;
    private final IndicadorRepository indicadorRepository;
    private final IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;

    // ============================================================================
    // EXPORT
    // ============================================================================

    /**
     * Recull, de manera recursiva (fórmula -> components), tots els indicadors utilitzats pels widgets d'un
     * dashboard (widgets simples, gràfics -amb un o més indicadors- i taules -una columna per indicador-).
     */
    public List<IndicadorExport> collectIndicadorExports(DashboardEntity dashboard) {
        Map<Long, IndicadorEntity> collected = new LinkedHashMap<>();
        if (dashboard != null && dashboard.getItems() != null) {
            for (DashboardItemEntity item : dashboard.getItems()) {
                collectFromWidget(item != null ? item.getWidget() : null, collected);
            }
        }
        return collected.values().stream()
                .map(indicador -> dashboardExportMapper.toIndicadorExport(indicador, estadisticaClientHelper))
                .collect(Collectors.toList());
    }

    private void collectFromWidget(EstadisticaWidgetEntity widget, Map<Long, IndicadorEntity> collected) {
        if (widget == null) return;
        if (widget instanceof EstadisticaSimpleWidgetEntity) {
            addFromIndicadorTaula(((EstadisticaSimpleWidgetEntity) widget).getIndicadorInfo(), collected);
        } else if (widget instanceof EstadisticaGraficWidgetEntity) {
            List<IndicadorTaulaEntity> indicadorsInfo = ((EstadisticaGraficWidgetEntity) widget).getIndicadorsInfo();
            if (indicadorsInfo != null) indicadorsInfo.forEach(info -> addFromIndicadorTaula(info, collected));
        } else if (widget instanceof EstadisticaTaulaWidgetEntity) {
            List<IndicadorTaulaEntity> columnes = ((EstadisticaTaulaWidgetEntity) widget).getColumnes();
            if (columnes != null) columnes.forEach(columna -> addFromIndicadorTaula(columna, collected));
        }
    }

    private void addFromIndicadorTaula(IndicadorTaulaEntity indicadorTaula, Map<Long, IndicadorEntity> collected) {
        if (indicadorTaula == null) return;
        addIndicador(indicadorTaula.getIndicador(), collected);
    }

    private void addIndicador(IndicadorEntity indicador, Map<Long, IndicadorEntity> collected) {
        if (indicador == null || indicador.getId() == null || collected.containsKey(indicador.getId())) return;
        collected.put(indicador.getId(), indicador);
        if (IndicadorTipus.FORMULA.equals(indicador.getTipus())) {
            indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(indicador.getId())
                    .forEach(terme -> addIndicador(terme.getIndicadorComponent(), collected));
        }
    }

    // ============================================================================
    // IMPORT
    // ============================================================================

    /** Crea els indicadors de tipus FORMULA de la llista que encara no existeixin al seu entornApp destí. */
    public void importIndicadorsFormula(List<IndicadorExport> indicadorExports) {
        if (indicadorExports == null) return;
        indicadorExports.stream()
                .filter(indicadorExport -> IndicadorTipus.FORMULA.equals(indicadorExport.getTipus()))
                .forEach(this::importIndicadorFormula);
    }

    public void importIndicadorsFormula(DashboardExport dashboard, List<Conflict> conflicts) {
        this.importIndicadorsFormula(dashboard, conflicts, null);
    }

    public void importIndicadorsFormula(
            DashboardExport dashboard,
            List<Conflict> conflicts,
            Map<String, String> remappedCodis) {
        if (dashboard == null || dashboard.getIndicadors() == null) return;
        dashboard.getIndicadors().stream()
                .filter(indicadorExport -> IndicadorTipus.FORMULA.equals(indicadorExport.getTipus()))
                .forEach(indicadorExport -> importIndicadorFormula(dashboard, indicadorExport, conflicts, remappedCodis));
    }

    private void importIndicadorFormula(IndicadorExport indicadorExport) {
        importIndicadorFormula(null, indicadorExport, Collections.emptyList(), null);
    }

    private void importIndicadorFormula(
            DashboardExport dashboard,
            IndicadorExport indicadorExport,
            List<Conflict> conflicts,
            Map<String, String> remappedCodis) {
        String originalCodi = indicadorExport.getCodi();
        if (remappedCodis != null && remappedCodis.containsKey(originalCodi)) {
            String targetCodi = remappedCodis.get(originalCodi);
            if (!Objects.equals(originalCodi, targetCodi)) {
                updateDashboardWidgetsIndicadorCodi(dashboard, originalCodi, targetCodi);
                indicadorExport.setCodi(targetCodi);
            }
            return;
        }

        Long entornAppId = resolveEntornAppId(indicadorExport.getEntornCodi(), indicadorExport.getAppCodi());
        if (entornAppId == null) {
            // Ja validat prèviament a DashboardImportHelper#checkDashboardConflicts; no hauria de passar.
            log.warn("No s'ha pogut resoldre l'entornApp {} - {} per importar l'indicador {}",
                    indicadorExport.getEntornCodi(), indicadorExport.getAppCodi(), originalCodi);
            return;
        }

        App app = estadisticaClientHelper.appFindByCodi(indicadorExport.getAppCodi());
        Long appId = app != null ? app.getId() : null;

        Conflict conflict = findConflict(indicadorExport.getNom(), originalCodi, entornAppId, appId, conflicts);

        if (conflict != null) {
            switch (conflict.getOverwrite()) {
                case EMPRAR_EXISTENT:
                    IndicadorEntity existing = indicadorRepository
                            .findByCodiAndEntornAppId(originalCodi, entornAppId)
                            .orElse(null);
                    if (existing != null) {
                        if (remappedCodis != null) {
                            remappedCodis.put(originalCodi, originalCodi);
                        }
                    }
                    return;
                case SOBRESCRIURE:
                    IndicadorEntity existent = indicadorRepository
                            .findByCodiAndEntornAppId(originalCodi, entornAppId)
                            .orElse(null);
                    if (existent != null) {
                        if (remappedCodis != null) {
                            remappedCodis.put(originalCodi, originalCodi);
                        }
                        updateIndicadorEntityFromExport(existent, indicadorExport, entornAppId);
                        indicadorRepository.save(existent);

                        List<IndicadorFormulaTermeEntity> oldTermes = indicadorFormulaTermeRepository
                                .findByIndicadorFormulaIdOrderByOrdreAsc(existent.getId());
                        if (oldTermes != null && !oldTermes.isEmpty()) {
                            indicadorFormulaTermeRepository.deleteAll(oldTermes);
                        }
                        if (existent.getFormula() != null) {
                            existent.getFormula().clear();
                        }
                        saveFormulaTermes(existent, indicadorExport.getFormula(), entornAppId);
                        return;
                    }
                    break;
                default:
                    break;
            }
        } else {
            // Si no hi ha conflicte registrat però ja existeix per codi a la BDD, es reutilitza
            IndicadorEntity existing = indicadorRepository
                    .findByCodiAndEntornAppId(originalCodi, entornAppId)
                    .orElse(null);
            if (existing != null) {
                if (remappedCodis != null) {
                    remappedCodis.put(originalCodi, originalCodi);
                }
                return;
            }
            if (remappedCodis != null) {
                remappedCodis.put(originalCodi, originalCodi);
            }
        }

        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi(indicadorExport.getCodi());
        updateIndicadorEntityFromExport(indicador, indicadorExport, entornAppId);
        indicadorRepository.save(indicador);
        saveFormulaTermes(indicador, indicadorExport.getFormula(), entornAppId);
    }

    private void updateIndicadorEntityFromExport(
            IndicadorEntity indicador,
            IndicadorExport indicadorExport,
            Long entornAppId) {
        indicador.setNom(indicadorExport.getNom());
        indicador.setDescripcio(indicadorExport.getDescripcio());
        indicador.setEntornAppId(entornAppId);
        indicador.setFormat(indicadorExport.getFormat());
        indicador.setTipus(IndicadorTipus.FORMULA);
        indicador.setCompactable(Boolean.TRUE.equals(indicadorExport.getCompactable()));
        indicador.setTipusCompactacio(indicadorExport.getTipusCompactacio());
        if (indicadorExport.getIndicadorComptadorPerMitjanaCodi() != null) {
            indicadorRepository.findByCodiAndEntornAppId(indicadorExport.getIndicadorComptadorPerMitjanaCodi(), entornAppId)
                    .ifPresent(indicador::setIndicadorComptadorPerMitjana);
        } else {
            indicador.setIndicadorComptadorPerMitjana(null);
        }
    }

    private void saveFormulaTermes(
            IndicadorEntity indicador,
            List<IndicadorFormulaTermeExport> formula,
            Long entornAppId) {
        if (formula != null) {
            formula.forEach(termeExport -> {
                IndicadorEntity component = indicadorRepository
                        .findByCodiAndEntornAppId(termeExport.getIndicadorComponentCodi(), entornAppId)
                        .orElse(null);
                if (component == null) {
                    // Ja validat prèviament a DashboardImportHelper#checkDashboardConflicts; no hauria de passar.
                    log.warn("No s'ha trobat l'indicador component {} a l'entornApp {} per a la fórmula {}",
                            termeExport.getIndicadorComponentCodi(), entornAppId, indicador.getCodi());
                    return;
                }
                IndicadorFormulaTermeEntity terme = new IndicadorFormulaTermeEntity();
                terme.setIndicadorFormula(indicador);
                terme.setIndicadorComponent(component);
                terme.setOperador(termeExport.getOperador());
                terme.setOrdre(termeExport.getOrdre());
                indicadorFormulaTermeRepository.save(terme);
            });
        }
    }

    public void updateDashboardWidgetsIndicadorCodi(DashboardExport dashboard, String oldCodi, String newCodi) {
        if (dashboard == null || dashboard.getItems() == null || oldCodi == null || newCodi == null) return;
        for (DashboardItemExport item : dashboard.getItems()) {
            if (item == null || item.getWidget() == null) continue;
            EstadisticaWidgetExport widget = item.getWidget();
            if (widget instanceof EstadisticaSimpleWidgetExport) {
                EstadisticaSimpleWidgetExport w = (EstadisticaSimpleWidgetExport) widget;
                if (w.getIndicadorInfo() != null && Objects.equals(oldCodi, w.getIndicadorInfo().getIndicadorCodi())) {
                    w.getIndicadorInfo().setIndicadorCodi(newCodi);
                }
            } else if (widget instanceof EstadisticaGraficWidgetExport) {
                EstadisticaGraficWidgetExport w = (EstadisticaGraficWidgetExport) widget;
                if (w.getIndicadorInfo() != null && Objects.equals(oldCodi, w.getIndicadorInfo().getIndicadorCodi())) {
                    w.getIndicadorInfo().setIndicadorCodi(newCodi);
                }
                if (w.getIndicadorsInfo() != null) {
                    for (IndicadorTaulaExport info : w.getIndicadorsInfo()) {
                        if (info != null && Objects.equals(oldCodi, info.getIndicadorCodi())) {
                            info.setIndicadorCodi(newCodi);
                        }
                    }
                }
            } else if (widget instanceof EstadisticaTaulaWidgetExport) {
                EstadisticaTaulaWidgetExport w = (EstadisticaTaulaWidgetExport) widget;
                if (w.getColumnes() != null) {
                    for (IndicadorTaulaExport columna : w.getColumnes()) {
                        if (columna != null && Objects.equals(oldCodi, columna.getIndicadorCodi())) {
                            columna.setIndicadorCodi(newCodi);
                        }
                    }
                }
            }
        }
    }


    private Conflict findConflict(String nom, String codi, Long entornAppId, Long appId, List<Conflict> conflicts) {
        if (conflicts == null) return null;
        return conflicts.stream()
                .filter(c -> IndicadorExport.class.getSimpleName().equals(c.getTipo())
                        && !c.isBloquejant()
                        && (c.getCodi() != null ? Objects.equals(codi, c.getCodi()) : Objects.equals(nom, c.getTitol()))
                        && (entornAppId == null || c.getEntornAppId() == null || Objects.equals(entornAppId, c.getEntornAppId()))
                        && (appId == null || c.getAppId() == null || Objects.equals(appId, c.getAppId())))
                .findFirst()
                .orElse(null);
    }

    private Long resolveEntornAppId(String entornCodi, String appCodi) {
        if (entornCodi == null || appCodi == null) return null;
        Entorn entorn = estadisticaClientHelper.entornByCodi(entornCodi);
        App app = estadisticaClientHelper.appFindByCodi(appCodi);
        if (entorn == null || app == null) return null;
        EntornApp entornApp = estadisticaClientHelper.entornAppFindByAppAndEntorn(app.getId(), entorn.getId());
        return entornApp != null ? entornApp.getId() : null;
    }

}
