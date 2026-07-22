package es.caib.comanda.monitor.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import es.caib.comanda.base.config.Cues;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.monitor.logic.helper.MonitorClientHelper;
import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.logic.intf.service.MonitorService;
import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl extends BaseMutableResourceService<Monitor, Long, MonitorEntity> implements MonitorService {

    private final MonitorClientHelper monitorClientHelper;
    private final JmsTemplate jmsTemplate;

    @PostConstruct
    public void init() {
        register(Monitor.PERSPECTIVE_ENTORN_APP, new EntornAppPerspectiveApplicator(monitorClientHelper));
        register(Monitor.MONITOR_DELETE_ENTORN_APP_BY_MODUL_ACTION, new DeleteAlarmaConfigAction(jmsTemplate));
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
            if (entity.getEntornAppId() == null) {
                return;
            }
            EntornApp entornApp = monitorClientHelper.entornAppFindById(entity.getEntornAppId());
            if (entornApp != null) {
                resource.setApp(entornApp.getApp());
                resource.setEntorn(entornApp.getEntorn());
            }
        }
    }

    @AllArgsConstructor
    public class DeleteAlarmaConfigAction implements ActionExecutor<MonitorEntity, String, Monitor> {
        private JmsTemplate jmsTemplate;
        @Override
        public Monitor exec(String code, MonitorEntity entity, String params) throws ActionExecutionException {
            if (!"netejaEntornApp".equals(entity.getOperacio())) {
                throw new ActionExecutionException(Monitor.class, entity.getId(), Monitor.MONITOR_DELETE_ENTORN_APP_BY_MODUL_ACTION,
                    "El monitor enviat no pot executar la logica solicitada.");
            }
            String cua = getCuaPerModul(entity.getModul());
            if (cua == null) {
                log.warn("No hi ha cua per al mòdul {}", entity.getModul());
                throw new ActionExecutionException(Monitor.class, entity.getId(), Monitor.MONITOR_DELETE_ENTORN_APP_BY_MODUL_ACTION,
                    "El modul associat no esta implementar al microservei de monitor, soliciteu ajuda al programador");
            }
            jmsTemplate.convertAndSend(cua, new NetejaEntornAppMessage(entity.getEntornAppId()));
            log.info("Reintent de neteja encuat per entornApp {} mòdul {}", entity.getEntornAppId(), entity.getModul());
            entity.setOperacio("netejaEntornAppCompletat");
            return null;
        }

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
        }

        private String getCuaPerModul(ModulEnum modul) {
            if (modul == null) return null;
            switch (modul) {
                case SALUT: return Cues.CUA_NETEJA_SALUT;
                case TASCA: return Cues.CUA_NETEJA_TASQUES;
                case AVIS: return Cues.CUA_NETEJA_AVISOS;
                case ALARMES: return Cues.CUA_NETEJA_ALARMES;
                case ESTADISTICA: return Cues.CUA_NETEJA_ESTADISTICA;
                default: return null;
            }
        }
    }

}
