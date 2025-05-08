package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaTaulaWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaTaulaWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei EstadisticaTaulaWidgetService per gestionar les operacions sobre widgets de taula d'estadístiques.
 * Aquesta classe extén BaseReadonlyResourceService, proporcionant funcionalitats estàndard de gestió de recursos de només lectura.
 *
 * Aquesta implementació utilitza Lombok per al registre del logger i es gestiona com un component de servei dins del context de Spring.
 *
 * L'objectiu d'aquest servei és facilitar les operacions associades amb els widgets de taula per estadístiques, basant-se en el model i l'entitat corresponent.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaTaulaWidgetServiceImpl extends BaseReadonlyResourceService<EstadisticaTaulaWidget, Long, EstadisticaTaulaWidgetEntity> implements EstadisticaTaulaWidgetService {
    
}
