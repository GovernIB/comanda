package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.service.IndicadorTaulaService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei per gestionar widgets d'estadística simple.
 * Aquesta classe proporciona funcionalitats de només lectura per interactuar amb dades de widgets d'estadística simple.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class IndicadorTaulaServiceImpl extends BaseMutableResourceService<IndicadorTaula, Long, IndicadorTaulaEntity> implements IndicadorTaulaService {

}
