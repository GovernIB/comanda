package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardPreferit;
import es.caib.comanda.estadistica.logic.intf.service.DashboardPreferitService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardPreferitEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els dashboards preferits.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardPreferitServiceImpl extends BaseMutableResourceService<DashboardPreferit, Long, DashboardPreferitEntity> implements DashboardPreferitService {



}
