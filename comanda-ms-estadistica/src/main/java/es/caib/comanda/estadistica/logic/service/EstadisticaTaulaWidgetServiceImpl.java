package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaTaulaWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaTaulaWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EstadisticaTaulaWidgetServiceImpl extends BaseReadonlyResourceService<EstadisticaTaulaWidget, Long, EstadisticaTaulaWidgetEntity> implements EstadisticaTaulaWidgetService {
    
}
