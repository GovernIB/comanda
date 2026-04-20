package es.caib.comanda.monitor.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.monitor.logic.helper.MonitorClientHelper;
import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.logic.intf.service.MonitorService;
import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl extends BaseMutableResourceService<Monitor, Long, MonitorEntity> implements MonitorService {

    private final MonitorClientHelper monitorClientHelper;

    @PostConstruct
    public void init() {
        register(Monitor.PERSPECTIVE_ENTORN_APP, new EntornAppPerspectiveApplicator(monitorClientHelper));
    }

    @Override
    protected String namedFilterToSpringFilter(String name) {
        if (Objects.nonNull(name) && name.startsWith(Monitor.FILTER_BY_APP_NAMEDFILTER)){
            long appId = Long.parseLong(name.split(":")[1]);
            return springFilterEntornAppIdInIdsList(monitorClientHelper.findEntornAppIdsByAppId(appId));
        } else if (Objects.nonNull(name) && name.startsWith(Monitor.FILTER_BY_ENTORN_NAMEDFILTER)) {
            long entornId = Long.parseLong(name.split(":")[1]);
            return springFilterEntornAppIdInIdsList(monitorClientHelper.findEntornAppIdsByEntornId(entornId));
        }
        return null;
    }

    private String springFilterEntornAppIdInIdsList(List<Long> entornAppIds) {
        if (entornAppIds == null || entornAppIds.isEmpty()) {
            return FilterBuilder.equal(Monitor.Fields.entornAppId, "0").toString();
        }
        String idsCommaSeparated = entornAppIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return Monitor.Fields.entornAppId + " in(" + idsCommaSeparated + ")";
    }

    @AllArgsConstructor
    public static class EntornAppPerspectiveApplicator implements PerspectiveApplicator<MonitorEntity, Monitor> {
        private MonitorClientHelper monitorClientHelper;
        @Override
        public void applySingle(String code, MonitorEntity entity, Monitor resource) throws PerspectiveApplicationException {
            EntornApp entornApp = monitorClientHelper.entornAppFindById(entity.getEntornAppId());
            if (entornApp != null) {
                resource.setApp(entornApp.getApp());
                resource.setEntorn(entornApp.getEntorn());
            }
        }
    }

}
