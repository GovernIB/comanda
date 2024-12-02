package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Subsistema;
import es.caib.comanda.configuracio.logic.intf.service.SubsistemaService;
import es.caib.comanda.configuracio.persist.entity.SubsistemaEntity;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió de subsistemes.
 *
 * @author Limit Tecnologies
 */
@Service
public class SubsistemaServiceImpl extends BaseMutableResourceServiceImpl<Subsistema, Long, SubsistemaEntity> implements SubsistemaService {

}
