package es.caib.comanda.permisos.logic.service;

import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.permisos.logic.intf.model.Objecte;
import es.caib.comanda.permisos.logic.intf.service.ObjecteService;
import es.caib.comanda.permisos.persist.entity.ObjecteEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObjecteServiceImpl extends BaseMutableResourceService<Objecte, Long, ObjecteEntity> implements ObjecteService {

}
