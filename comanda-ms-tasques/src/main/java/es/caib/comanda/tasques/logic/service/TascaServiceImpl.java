package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.tasques.logic.helper.MonitorTasques;
import es.caib.comanda.tasques.logic.helper.TasquesClientHelper;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.logic.intf.service.TascaService;
import es.caib.comanda.tasques.logic.mapper.TascaMapper;
import es.caib.comanda.tasques.persist.entity.TascaEntity;
import es.caib.comanda.tasques.persist.repository.TascaRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.*;

import static es.caib.comanda.base.config.BaseConfig.ROLE_ADMIN;
import static es.caib.comanda.base.config.Cues.CUA_TASQUES;

@Slf4j
@Service
@RequiredArgsConstructor
public class TascaServiceImpl extends BaseMutableResourceService<Tasca, Long, TascaEntity> implements TascaService {

    private final AuthenticationHelper authenticationHelper;
    private final TasquesClientHelper tasquesClientHelper;
    private final TascaMapper tascaMapper;

    @PostConstruct
    public void init() {
        register(Tasca.PERSPECTIVE_PATH, new PathPerspectiveApplicator(tasquesClientHelper));
        register(Tasca.PERSPECTIVE_ENTORN_APP, new EntornAppPerspectiveApplicator(tasquesClientHelper));
        register(Tasca.PERSPECTIVE_EXPIRATION, new ExpirationPerspectiveApplicator());
    }

    @JmsListener(destination = CUA_TASQUES)
    @Transactional
    public void receiveMessage(@Payload es.caib.comanda.model.v1.tasca.Tasca tascaBroker,
                               Message message) throws JMSException {
        MonitorTasques monitorTasques = new MonitorTasques(null, "", tasquesClientHelper);
        monitorTasques.startAction();
        try {
            message.acknowledge();
            log.debug("Processat tasca de la cua " + CUA_TASQUES + " (tasca={})", tascaBroker.getIdentificador());
            Optional<EntornApp> entornApp = tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi(
                    tascaBroker.getEntornCodi(),
                    tascaBroker.getAppCodi());
            if (entornApp.isEmpty()) {
                throw new ResourceNotFoundException(
                        EntornApp.class,
                        "(entornCodi=" + tascaBroker.getEntornCodi() + ", appCodi=" + tascaBroker.getAppCodi() + ")");
            }
            monitorTasques.getMonitor().setEntornAppId(entornApp.get().getId());
            Optional<TascaEntity> tascaExistent = ((TascaRepository)entityRepository).findByEntornAppIdAndIdentificador(
                    entornApp.get().getId(),
                    tascaBroker.getIdentificador());

            if (Boolean.TRUE.equals(tascaBroker.getEsborrar())) {
                if (tascaExistent.isPresent()) {
                    entityRepository.delete(tascaExistent.get());
                    log.debug("Esborrada tasca {} de l'entornApp {}", tascaBroker.getIdentificador(), entornApp.get().getId());
                } else {
                    log.warn("S'ha intentat esborrar una tasca que no existeix: {} de l'entornApp {}", tascaBroker.getIdentificador(), entornApp.get().getId());
                }
                return;
            }

            Tasca tasca = tascaMapper.toTasca(tascaBroker, entornApp.get());
            if (tascaExistent.isEmpty()) {
                monitorTasques.setCreateActionMessatge();
                TascaEntity entity = tascaMapper.toTascaEntity(tasca);
                entityRepository.save(entity);
            } else {
                monitorTasques.setUpdateActionMessatge();
                tascaMapper.updateTasca(tasca, tascaExistent.get());
            }
            monitorTasques.endAction();
        } catch (Throwable t) {
            monitorTasques.endAction(t, t.getMessage());
        }

    }

    private static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert != null ? dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime() : null;
    }

    @Override
    protected void afterConversion(TascaEntity entity, Tasca resource) {
        super.afterConversion(entity, resource);

        App app = tasquesClientHelper.appById(entity.getAppId());
        Entorn entorn = tasquesClientHelper.entornById(entity.getEntornId());
        resource.setAppCodi(app != null ? app.getCodi() : null);
        resource.setEntornCodi(entorn != null ? entorn.getCodi() : null);
    }

    @Override
    protected Specification<TascaEntity> additionalSpecification(String[] namedQueries) {
        String userName = authenticationHelper.getCurrentUserName();
        String[] roles = authenticationHelper.getCurrentUserRealmRoles();

        // Els usuaris amb rol admin poden visualitzar totes les tasques.
        // Per a aquest motiu, l'usuari de httpauth.username ha de tenir el rol ROLE_ADMIN,
        // ja que totes les tasques han de ser accessibles mitjançant API interna.
        if (Arrays.asList(roles).contains(ROLE_ADMIN))
            return null;

        return teGrupSiNoNull(roles).and(
                teResponsable(userName).
                        or(tePermisUsuari(userName)).
                        or(tePermisGrupIn(roles)));
    }

    public static Specification<TascaEntity> teGrupSiNoNull(String[] grups) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("grup")),
                root.get("grup").in(Arrays.asList(grups))
        );
    }
    private Specification<TascaEntity> teResponsable(String responsable) {
        return (root, query, cb) -> cb.equal(root.get("responsable"), responsable);
    }
    private Specification<TascaEntity> tePermisUsuari(String usuari) {
        return (root, query, cb) -> {
            Join<TascaEntity, String> join = root.join("usuarisAmbPermis", JoinType.LEFT);
            query.distinct(true);
            return cb.equal(join, usuari);
        };
    }
    private Specification<TascaEntity> tePermisGrupIn(String[] grups) {
        return (root, query, cb) -> {
            Join<TascaEntity, String> join = root.join("grupsAmbPermis", JoinType.LEFT);
            query.distinct(true);
            return join.in(Arrays.asList(grups));
        };
    }

    @AllArgsConstructor
    public static class PathPerspectiveApplicator implements PerspectiveApplicator<TascaEntity, Tasca> {
        private TasquesClientHelper tasquesClientHelper;
        @Override
        public void applySingle(String code, TascaEntity entity, Tasca resource) throws PerspectiveApplicationException {
            EntornApp entornApp = tasquesClientHelper.entornAppFindById(entity.getEntornAppId());
            if (entornApp != null) {
                resource.setTreePath(new String[]{entornApp.getApp().getNom(), entornApp.getEntorn().getNom(), resource.getIdentificador()});
            } else {
                resource.setTreePath(new String[]{"INVALID_ENTORNAPP " + entity.getEntornAppId(), resource.getIdentificador()});
            }
        }
    }

    @AllArgsConstructor
    public static class EntornAppPerspectiveApplicator implements PerspectiveApplicator<TascaEntity, Tasca> {
        private TasquesClientHelper tasquesClientHelper;
        @Override
        public void applySingle(String code, TascaEntity entity, Tasca resource) throws PerspectiveApplicationException {
            EntornApp entornApp = tasquesClientHelper.entornAppFindById(entity.getEntornAppId());
            if (entornApp != null) {
                resource.setApp(entornApp.getApp());
                resource.setEntorn(entornApp.getEntorn());
            }
        }
    }

    public static class ExpirationPerspectiveApplicator implements PerspectiveApplicator<TascaEntity, Tasca> {
        @Override
        public void applySingle(String code, TascaEntity entity, Tasca resource) throws PerspectiveApplicationException {
            if (resource.getDataFi() == null && resource.getDataCaducitat() != null) {
                long diesPerCaducar = ChronoUnit.DAYS.between(
                        LocalDateTime.now(),
                        resource.getDataCaducitat());
                resource.setDiesPerCaducar(diesPerCaducar);
            }
        }
    }

}
