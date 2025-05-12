package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.logic.intf.service.MonitorService;
import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MonitorServiceImpl extends BaseMutableResourceService<Monitor, Long, MonitorEntity> implements MonitorService {

}
