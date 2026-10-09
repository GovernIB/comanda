package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaNetejaService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorFormulaTermeRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementació del servei de neteja de dades estadístiques associades a una aplicació o aplicació-entorn.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EstadisticaNetejaServiceImpl implements EstadisticaNetejaService {

    private final FetRepository fetRepository;
    private final IndicadorTaulaRepository indicadorTaulaRepository;
    private final IndicadorRepository indicadorRepository;
    private final IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final DimensioRepository dimensioRepository;
    private final DashboardItemRepository dashboardItemRepository;
    private final DashboardRepository dashboardRepository;
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final CacheHelper cacheHelper;

    @Override
    public void netejaPerEntornApp(Long entornAppId) {
        log.info("Iniciant neteja de dades estadístiques (només fets) per entornApp {}", entornAppId);
        fetRepository.deleteByEntornAppId(entornAppId);
        cacheHelper.evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
        log.info("Neteja completada per entornApp {} (només dades/fets)", entornAppId);
    }

    @Override
    public void netejaPerEntornAppEsborrat(Long entornAppId, Long appId, Long entornId) {
        log.info("Iniciant neteja completa de dades estadístiques per entornApp esborrat {} (app: {}, entorn: {})",
                entornAppId, appId, entornId);

        // 1. Fets
        fetRepository.deleteByEntornAppId(entornAppId);

        // 2. Dashboards i elements de dashboard de l'app a l'entorn. Es guarden els widgets afectats per
        // esborrar-los si es queden sense cap element de dashboard.
        Set<Long> widgetIds = new LinkedHashSet<>();
        if (appId != null && entornId != null) {
            esborrarDashboardsIElements(appId, entornId, widgetIds);
        } else {
            log.warn("Neteja de l'entornApp {} sense app/entorn: no s'esborren dashboards ni elements de dashboard", entornAppId);
        }

        // 3. Widgets que referencien el catàleg de l'entornApp o que s'han quedat sense elements
        Set<Long> widgetIdsAmbCataleg = new LinkedHashSet<>(estadisticaWidgetRepository.findIdsByCatalegEntornAppId(entornAppId));
        widgetIds.addAll(widgetIdsAmbCataleg);
        for (Long widgetId : widgetIds) {
            estadisticaWidgetRepository.findById(widgetId).ifPresent(widget -> {
                List<DashboardItemEntity> elementsRestants = dashboardItemRepository.findByWidgetId(widgetId);
                if (elementsRestants.isEmpty()) {
                    log.debug("Esborrant el widget {} perquè no s'utilitza en cap altre dashboard", widgetId);
                    estadisticaWidgetRepository.delete(widget);
                } else if (widgetIdsAmbCataleg.contains(widgetId)) {
                    reapuntarWidgetACatalegAlternatiu((EstadisticaWidgetEntity<?>) Hibernate.unproxy(widget), elementsRestants, entornAppId);
                }
            });
        }
        estadisticaWidgetRepository.flush();

        // 4. Desvincular les referències que hagin quedat cap al catàleg i esborrar-lo
        estadisticaWidgetRepository.clearIndicadorByEntornAppId(entornAppId);
        estadisticaWidgetRepository.clearTaulaDimensioAgrupacioByEntornAppId(entornAppId);
        estadisticaWidgetRepository.clearGraficDescomposicioDimensioByEntornAppId(entornAppId);
        estadisticaWidgetRepository.deleteWidgetDimValorByEntornAppId(entornAppId);
        esborrarCataleg(entornAppId);

        cacheHelper.evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
        log.info("Neteja completa finalitzada per entornApp esborrat {}", entornAppId);
    }

    @Override
    public void netejaPerApp(Long appId, List<Long> entornAppIds, boolean esborrarCataleg, boolean esborrarWidgets) {
        log.info("Iniciant neteja d'estadístiques per appId {} (entorns: {}, esborrarCataleg: {}, esborrarWidgets: {})",
                appId, entornAppIds, esborrarCataleg, esborrarWidgets);

        // 1. Esborrar fets de tots els entorns associats
        if (entornAppIds != null) {
            for (Long entornAppId : entornAppIds) {
                fetRepository.deleteByEntornAppId(entornAppId);
            }
        }

        // Si no s'esborra el catàleg, ja hem acabat (només volíem esborrar fets)
        if (!esborrarCataleg) {
            cacheHelper.evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
            log.info("Neteja completada per appId {} (només dades/fets)", appId);
            return;
        }

        // 2. Gestió de widgets si s'esborra el catàleg
        if (esborrarWidgets) {
            dashboardItemRepository.deleteByWidgetAppId(appId);
            estadisticaWidgetRepository.deleteWidgetDimValorByAppId(appId);
            indicadorTaulaRepository.deleteByWidgetAppId(appId);
            estadisticaWidgetRepository.deleteByAppId(appId);
        } else {
            // Si NO s'esborren els widgets, es desvinculen de les dimensions i indicadors dels entorns
            if (entornAppIds != null) {
                for (Long entornAppId : entornAppIds) {
                    estadisticaWidgetRepository.clearIndicadorByEntornAppId(entornAppId);
                    estadisticaWidgetRepository.clearTaulaDimensioAgrupacioByEntornAppId(entornAppId);
                    estadisticaWidgetRepository.clearGraficDescomposicioDimensioByEntornAppId(entornAppId);
                    estadisticaWidgetRepository.deleteWidgetDimValorByEntornAppId(entornAppId);
                }
            }
        }

        // 3. Esborrar catàleg de cada entorn de l'app en ordre FK
        if (entornAppIds != null) {
            for (Long entornAppId : entornAppIds) {
                esborrarCataleg(entornAppId);
            }
        }

        cacheHelper.evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
        log.info("Neteja completada per appId {} (amb catàleg, esborrarWidgets: {})", appId, esborrarWidgets);
    }

    /** Esborra el catàleg (indicadors i dimensions) d'un entornApp en ordre de claus foranes. */
    private void esborrarCataleg(Long entornAppId) {
        indicadorTaulaRepository.deleteByIndicadorEntornAppId(entornAppId);
        indicadorFormulaTermeRepository.deleteByEntornAppId(entornAppId);
        indicadorRepository.clearCompactacioByEntornAppId(entornAppId);
        indicadorRepository.deleteByEntornAppId(entornAppId);
        dimensioValorRepository.deleteByDimensioEntornAppId(entornAppId);
        dimensioRepository.deleteByEntornAppId(entornAppId);
    }

    /**
     * Esborra els dashboards de l'app a l'entorn (en cascada: elements, títols, filtres i preferits) i els elements
     * d'altres dashboards que mostren widgets de l'app a l'entorn. Afegeix a {@code widgetIds} els widgets afectats.
     */
    private void esborrarDashboardsIElements(Long appId, Long entornId, Set<Long> widgetIds) {
        List<DashboardEntity> dashboards = dashboardRepository.findByAppIdAndEntornId(appId, entornId);
        Set<Long> dashboardIds = new LinkedHashSet<>();
        for (DashboardEntity dashboard : dashboards) {
            dashboardIds.add(dashboard.getId());
            if (dashboard.getItems() != null) {
                dashboard.getItems().stream()
                        .map(DashboardItemEntity::getWidget)
                        .filter(Objects::nonNull)
                        .forEach(widget -> widgetIds.add(widget.getId()));
            }
        }
        List<DashboardItemEntity> elements = dashboardItemRepository.findByEntornIdAndWidgetAppId(entornId, appId).stream()
                .filter(item -> item.getDashboard() == null || !dashboardIds.contains(item.getDashboard().getId()))
                .collect(Collectors.toList());
        elements.forEach(item -> widgetIds.add(item.getWidget().getId()));
        log.debug("Esborrant {} dashboards i {} elements d'altres dashboards de l'app {} a l'entorn {}",
                dashboards.size(), elements.size(), appId, entornId);
        dashboardItemRepository.deleteAll(elements);
        dashboardRepository.deleteAll(dashboards);
        dashboardRepository.flush();
    }

    /**
     * Reapunta les referències d'un widget al catàleg de l'entornApp esborrat cap als elements equivalents (mateix codi)
     * del catàleg d'un altre entornApp en què el widget encara es mostra. Les referències sense equivalent es deixen
     * tal qual: s'esborren o es desvinculen en esborrar el catàleg.
     */
    private void reapuntarWidgetACatalegAlternatiu(EstadisticaWidgetEntity<?> widget, List<DashboardItemEntity> elementsRestants, Long entornAppId) {
        Long entornAppDestiId = cercarEntornAppDesti(widget.getAppId(), elementsRestants, entornAppId);
        if (entornAppDestiId == null) {
            log.warn("El widget {} encara s'utilitza però no s'ha trobat cap entornApp alternatiu: es desvincula del catàleg de l'entornApp {}",
                    widget.getId(), entornAppId);
            return;
        }
        log.debug("Reapuntant el widget {} del catàleg de l'entornApp {} al de l'entornApp {}", widget.getId(), entornAppId, entornAppDestiId);
        for (IndicadorTaulaEntity indicadorTaula : indicadorTaulaRepository.findByWidgetIdAndIndicadorEntornAppId(widget.getId(), entornAppId)) {
            indicadorRepository.findByCodiAndEntornAppId(indicadorTaula.getIndicador().getCodi(), entornAppDestiId)
                    .ifPresent(indicadorTaula::setIndicador);
        }
        if (widget instanceof EstadisticaTaulaWidgetEntity) {
            EstadisticaTaulaWidgetEntity taula = (EstadisticaTaulaWidgetEntity) widget;
            taula.setDimensioAgrupacio(dimensioEquivalent(taula.getDimensioAgrupacio(), entornAppId, entornAppDestiId));
        } else if (widget instanceof EstadisticaGraficWidgetEntity) {
            EstadisticaGraficWidgetEntity grafic = (EstadisticaGraficWidgetEntity) widget;
            grafic.setDescomposicioDimensio(dimensioEquivalent(grafic.getDescomposicioDimensio(), entornAppId, entornAppDestiId));
        }
        if (widget.getDimensionsValor() != null) {
            List<DimensioValorEntity> dimensionsValor = new ArrayList<>();
            for (DimensioValorEntity dimensioValor : widget.getDimensionsValor()) {
                if (!Objects.equals(dimensioValor.getDimensio().getEntornAppId(), entornAppId)) {
                    dimensionsValor.add(dimensioValor);
                    continue;
                }
                dimensioRepository.findByCodiAndEntornAppId(dimensioValor.getDimensio().getCodi(), entornAppDestiId)
                        .flatMap(dimensio -> dimensioValorRepository.findByDimensioAndValor(dimensio, dimensioValor.getValor()))
                        .ifPresent(dimensionsValor::add);
            }
            widget.getDimensionsValor().clear();
            widget.getDimensionsValor().addAll(dimensionsValor);
        }
    }

    /** Retorna la dimensió equivalent a l'entornApp destí si la dimensió pertany a l'entornApp esborrat. */
    private DimensioEntity dimensioEquivalent(DimensioEntity dimensio, Long entornAppId, Long entornAppDestiId) {
        if (dimensio == null || !Objects.equals(dimensio.getEntornAppId(), entornAppId)) {
            return dimensio;
        }
        return dimensioRepository.findByCodiAndEntornAppId(dimensio.getCodi(), entornAppDestiId).orElse(null);
    }

    /** Cerca l'entornApp d'algun dels entorns en què encara es mostra el widget. */
    private Long cercarEntornAppDesti(Long appId, List<DashboardItemEntity> elementsRestants, Long entornAppId) {
        List<Long> entornIds = elementsRestants.stream()
                .map(DashboardItemEntity::getEntornId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        for (Long entornId : entornIds) {
            try {
                Long entornAppDestiId = Optional.ofNullable(estadisticaClientHelper.entornAppFindByAppAndEntorn(appId, entornId))
                        .map(EntornApp::getId)
                        .orElse(null);
                if (entornAppDestiId != null && !entornAppDestiId.equals(entornAppId)) {
                    return entornAppDestiId;
                }
            } catch (Exception e) {
                log.warn("No s'ha pogut obtenir l'entornApp de l'app {} a l'entorn {}", appId, entornId, e);
            }
        }
        return null;
    }

}
