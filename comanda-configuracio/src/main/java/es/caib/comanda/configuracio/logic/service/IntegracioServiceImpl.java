package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Integracio;
import es.caib.comanda.configuracio.logic.intf.service.IntegracioService;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió d'integracions.
 *
 * @author Limit Tecnologies
 */
@Service
public class IntegracioServiceImpl extends BaseMutableResourceService<Integracio, Long, IntegracioEntity> implements IntegracioService {

}
