package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.DashboardItemTitolHelper;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.logic.intf.service.DashboardTitolService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els títols de dashboards.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardTitolServiceImpl extends BaseMutableResourceService<DashboardTitol, Long, DashboardTitolEntity> implements DashboardTitolService {

	private final DashboardItemTitolHelper dashboardItemTitolHelper;

	@Override
	protected void completeResource(DashboardTitol resource) {
		dashboardItemTitolHelper.completeResourceTitolLogic(resource);
	}

}
