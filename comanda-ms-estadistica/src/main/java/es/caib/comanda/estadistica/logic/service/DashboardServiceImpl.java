package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.Dimensio;
import es.caib.comanda.estadistica.logic.intf.service.DashboardService;
import es.caib.comanda.estadistica.logic.intf.service.DimensioService;
import es.caib.comanda.estadistica.persist.entity.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DashboardServiceImpl extends BaseReadonlyResourceService<Dashboard, Long, DashboardEntity> implements DashboardService {

}
