package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei per gestionar widgets d'estadística simple.
 * Aquesta classe proporciona funcionalitats de només lectura per interactuar amb dades de widgets d'estadística simple.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaSimpleWidgetServiceImpl extends BaseReadonlyResourceService<EstadisticaSimpleWidget, Long, EstadisticaSimpleWidgetEntity> implements EstadisticaSimpleWidgetService {
    
}
