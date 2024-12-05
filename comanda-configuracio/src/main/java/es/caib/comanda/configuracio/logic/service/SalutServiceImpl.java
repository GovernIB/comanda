package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Salut;
import es.caib.comanda.configuracio.logic.intf.service.SalutService;
import es.caib.comanda.configuracio.persist.entity.SalutEntity;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de consulta d'informació de salut.
 *
 * @author Limit Tecnologies
 */
@Service
public class SalutServiceImpl extends BaseReadonlyResourceService<Salut, Long, SalutEntity> implements SalutService {

}
