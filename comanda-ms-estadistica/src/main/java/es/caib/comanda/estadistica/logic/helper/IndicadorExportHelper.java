package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.export.IndicadorExport;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private void importIndicadorFormula(IndicadorExport indicadorExport) {
        Long entornAppId = resolveEntornAppId(indicadorExport.getEntornCodi(), indicadorExport.getAppCodi());
        if (entornAppId == null) {
            // Ja validat prèviament a DashboardImportHelper#checkDashboardConflicts; no hauria de passar.
            log.warn("No s'ha pogut resoldre l'entornApp {} - {} per importar l'indicador {}",
                    indicadorExport.getEntornCodi(), indicadorExport.getAppCodi(), indicadorExport.getCodi());
            return;
        }
        if (indicadorRepository.findByCodiAndEntornAppId(indicadorExport.getCodi(), entornAppId).isPresent()) {
            return; // Ja existeix: es reutilitza (identificat pel seu codi dins l'entornApp).
        }

        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi(indicadorExport.getCodi());
        indicador.setNom(indicadorExport.getNom());
        indicador.setDescripcio(indicadorExport.getDescripcio());
        indicador.setEntornAppId(entornAppId);
        indicador.setFormat(indicadorExport.getFormat());
        indicador.setTipus(IndicadorTipus.FORMULA);
        indicador.setCompactable(Boolean.TRUE.equals(indicadorExport.getCompactable()));
        indicador.setTipusCompactacio(indicadorExport.getTipusCompactacio());
        indicadorRepository.save(indicador);

        if (indicadorExport.getFormula() != null) {
            indicadorExport.getFormula().forEach(termeExport -> {
                IndicadorEntity component = indicadorRepository
                        .findByCodiAndEntornAppId(termeExport.getIndicadorComponentCodi(), entornAppId)
                        .orElse(null);
                if (component == null) {
                    // Ja validat prèviament a DashboardImportHelper#checkDashboardConflicts; no hauria de passar.
                    log.warn("No s'ha trobat l'indicador component {} a l'entornApp {} per a la fórmula {}",
                            termeExport.getIndicadorComponentCodi(), entornAppId, indicadorExport.getCodi());
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

    private Long resolveEntornAppId(String entornCodi, String appCodi) {
        if (entornCodi == null || appCodi == null) return null;
        Entorn entorn = estadisticaClientHelper.entornByCodi(entornCodi);
        App app = estadisticaClientHelper.appFindByCodi(appCodi);
        if (entorn == null || app == null) return null;
        EntornApp entornApp = estadisticaClientHelper.entornAppFindByAppAndEntorn(app.getId(), entorn.getId());
        return entornApp != null ? entornApp.getId() : null;
    }

}
