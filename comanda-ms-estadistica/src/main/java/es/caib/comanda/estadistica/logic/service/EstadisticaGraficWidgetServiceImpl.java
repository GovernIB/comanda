package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaGraficWidgetEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EstadisticaGraficWidgetServiceImpl extends BaseReadonlyResourceService<EstadisticaGraficWidget, Long, EstadisticaGraficWidgetEntity> implements EstadisticaGraficWidgetService {
    
}
