package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.service.EstadisticaNetejaService;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementació del servei de neteja de dades estadístiques associades a una aplicació-entorn.
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
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;
    private final IndicadorRepository indicadorRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final DimensioRepository dimensioRepository;

    @Override
    public void netejaPerEntornApp(Long entornAppId) {
        log.debug("Neteja de dades estadístiques per entornApp {}", entornAppId);
        indicadorTaulaRepository.deleteByIndicadorEntornAppId(entornAppId);
        fetRepository.deleteByEntornAppId(entornAppId);
        estadisticaWidgetRepository.deleteAll(estadisticaWidgetRepository.findByAppId(entornAppId));
        indicadorRepository.deleteByEntornAppId(entornAppId);
        dimensioValorRepository.deleteByDimensioEntornAppId(entornAppId);
        dimensioRepository.deleteByEntornAppId(entornAppId);
    }

}
