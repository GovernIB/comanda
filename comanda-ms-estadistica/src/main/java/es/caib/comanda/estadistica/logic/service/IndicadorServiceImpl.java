package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.Indicador;
import es.caib.comanda.estadistica.logic.intf.service.IndicadorService;
import es.caib.comanda.estadistica.persist.entity.IndicadorEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IndicadorServiceImpl extends BaseReadonlyResourceService<Indicador, Long, IndicadorEntity> implements IndicadorService {
    
}
