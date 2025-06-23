package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Integracio;
import es.caib.comanda.configuracio.logic.intf.service.IntegracioService;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
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
public class IntegracioServiceImpl extends BaseMutableResourceService<Integracio, Long, IntegracioEntity> implements IntegracioService {

}
