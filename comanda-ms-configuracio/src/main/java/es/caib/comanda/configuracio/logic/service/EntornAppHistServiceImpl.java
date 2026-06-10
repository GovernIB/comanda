package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.EntornAppHist;
import es.caib.comanda.configuracio.logic.intf.service.EntornAppHistService;
import es.caib.comanda.configuracio.persist.entity.EntornAppHistEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió d'històric de canvis externs d'entorn d'aplicació.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EntornAppHistServiceImpl extends BaseMutableResourceService<EntornAppHist, Long, EntornAppHistEntity> implements EntornAppHistService {

}
