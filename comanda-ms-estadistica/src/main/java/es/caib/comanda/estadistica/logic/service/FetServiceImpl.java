package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.estadistica.persist.entity.FetEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FetServiceImpl extends BaseReadonlyResourceService<Fet, Long, FetEntity> implements FetService {
    
}
