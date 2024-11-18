package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió d'aplicacions.
 *
 * @author Limit Tecnologies
 */
@Service
public class AppServiceImpl extends BaseMutableResourceServiceImpl<App, Long, AppEntity> implements AppService {

}
