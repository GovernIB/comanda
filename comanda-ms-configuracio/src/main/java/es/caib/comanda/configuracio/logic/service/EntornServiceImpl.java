package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.intf.service.EntornService;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió d'entorns.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EntornServiceImpl extends BaseMutableResourceService<Entorn, Long, EntornEntity> implements EntornService {

}
