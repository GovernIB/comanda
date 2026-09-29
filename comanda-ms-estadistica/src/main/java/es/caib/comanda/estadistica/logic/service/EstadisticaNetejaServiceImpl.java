package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.service.EstadisticaNetejaService;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
    private final CacheHelper cacheHelper;

    @Override
    public void netejaPerEntornApp(Long entornAppId) {
        log.info("Iniciant neteja de dades estadístiques (només fets) per entornApp {}", entornAppId);
        fetRepository.deleteByEntornAppId(entornAppId);
        cacheHelper.evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
        log.info("Neteja completada per entornApp {} (només dades/fets)", entornAppId);
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
                indicadorTaulaRepository.deleteByIndicadorEntornAppId(entornAppId);
                indicadorFormulaTermeRepository.deleteByEntornAppId(entornAppId);
                indicadorRepository.clearCompactacioByEntornAppId(entornAppId);
                indicadorRepository.deleteByEntornAppId(entornAppId);
                dimensioValorRepository.deleteByDimensioEntornAppId(entornAppId);
                dimensioRepository.deleteByEntornAppId(entornAppId);
            }
        }

        cacheHelper.evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
        log.info("Neteja completada per appId {} (amb catàleg, esborrarWidgets: {})", appId, esborrarWidgets);
    }

}
