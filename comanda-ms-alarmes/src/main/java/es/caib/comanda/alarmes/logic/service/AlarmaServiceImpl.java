package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmaServiceImpl extends BaseMutableResourceService<Alarma, Long, AlarmaEntity> implements AlarmaService {

}
