package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorFormulaTerme;
import es.caib.comanda.estadistica.logic.intf.service.IndicadorFormulaTermeService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei per gestionar els termes de fórmula d'indicadors.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class IndicadorFormulaTermeServiceImpl extends BaseMutableResourceService<IndicadorFormulaTerme, Long, IndicadorFormulaTermeEntity> implements IndicadorFormulaTermeService {
}
