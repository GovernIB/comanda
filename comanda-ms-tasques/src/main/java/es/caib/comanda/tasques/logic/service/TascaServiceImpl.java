package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.tasques.logic.helper.TasquesClientHelper;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.logic.intf.service.TascaService;
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

import static es.caib.comanda.base.config.Cues.CUA_TASQUES;

@Slf4j
@Service
@RequiredArgsConstructor
public class TascaServiceImpl extends BaseMutableResourceService<Tasca, Long, TascaEntity> implements TascaService {

    private final AuthenticationHelper authenticationHelper;
    private final TasquesClientHelper tasquesClientHelper;

    @PostConstruct
    public void init() {
        register(Tasca.PERSPECTIVE_PATH, new PathPerspectiveApplicator(tasquesClientHelper));
        register(Tasca.PERSPECTIVE_EXPIRATION, new ExpirationPerspectiveApplicator());
    }

    @JmsListener(destination = CUA_TASQUES)
    @Transactional
    public void receiveMessage(@Payload es.caib.comanda.model.v1.tasca.Tasca tascaBroker,
                               Message message) throws JMSException {
        message.acknowledge();
        log.debug("Processat tasca de la cua " + CUA_TASQUES + " (tasca={})", tascaBroker);
        Optional<EntornApp> entornApp = tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi(
                tascaBroker.getEntornCodi(),
                tascaBroker.getAppCodi());
        if (entornApp.isEmpty()) {
            throw new ResourceNotFoundException(
                    EntornApp.class,
                    "(entornCodi=" + tascaBroker.getEntornCodi() + ", appCodi=" + tascaBroker.getAppCodi() + ")");
        }
        Optional<TascaEntity> tascaExistent = ((TascaRepository)entityRepository).findByEntornAppIdAndIdentificador(
                entornApp.get().getId(),
                tascaBroker.getIdentificador());
        Tasca tasca = new Tasca();
        tasca.setEntornAppId(entornApp.get().getId());
        tasca.setEntornId(entornApp.get().getEntorn().getId());
        tasca.setAppId(entornApp.get().getApp().getId());
        tasca.setIdentificador(tascaBroker.getIdentificador());
        tasca.setTipus(tascaBroker.getTipus());
        tasca.setNom(tascaBroker.getNom());
        tasca.setDescripcio(tascaBroker.getDescripcio());
        tasca.setEstat(tascaBroker.getEstat());
        tasca.setEstatDescripcio(tascaBroker.getEstatDescripcio());
        tasca.setPrioritat(tascaBroker.getPrioritat());
        tasca.setDataInici(tascaBroker.getDataInici() != null ? tascaBroker.getDataInici().toLocalDateTime() : null);
        tasca.setDataFi(tascaBroker.getDataFi() != null ? tascaBroker.getDataFi().toLocalDateTime() : null);
        tasca.setDataCaducitat(tascaBroker.getDataCaducitat() != null ? tascaBroker.getDataCaducitat().toLocalDateTime() : null);
        tasca.setUrl(tascaBroker.getRedireccio());
        tasca.setResponsable(tascaBroker.getResponsable());
        tasca.setGrup(tascaBroker.getGrup());
        tasca.setUsuarisAmbPermis(tascaBroker.getUsuarisAmbPermis());
        tasca.setGrupsAmbPermis(tascaBroker.getGrupsAmbPermis());
        if (tascaExistent.isEmpty()) {
            TascaEntity entity = TascaEntity.builder().tasca(tasca).build();
            entityRepository.save(entity);
        } else {
            tascaExistent.get().setTipus(tasca.getTipus());
            tascaExistent.get().setNom(tasca.getNom());
            tascaExistent.get().setDescripcio(tasca.getDescripcio());
            tascaExistent.get().setEstat(tasca.getEstat());
            tascaExistent.get().setEstatDescripcio(tasca.getEstatDescripcio());
            tascaExistent.get().setPrioritat(tasca.getPrioritat());
            tascaExistent.get().setDataInici(tasca.getDataInici());
            tascaExistent.get().setDataFi(tasca.getDataFi());
            tascaExistent.get().setDataCaducitat(tasca.getDataCaducitat());
            tascaExistent.get().setUrl(tasca.getUrl());
            tascaExistent.get().setResponsable(tasca.getResponsable());
            tascaExistent.get().setGrup(tasca.getGrup());
            tascaExistent.get().setUsuarisAmbPermis(tasca.getUsuarisAmbPermis());
            tascaExistent.get().setGrupsAmbPermis(tasca.getGrupsAmbPermis());
        }
    }

    private static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert != null ? dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime() : null;
    }
    
    /*@Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
        List<Filter> filters = new ArrayList<>();
        if (currentSpringFilter != null && !currentSpringFilter.isEmpty()) {
            filters.add(Filter.parse(currentSpringFilter));
        }
        //if (namedQueries != null && Arrays.asList(namedQueries).contains("USER")) {
            String userName = authenticationHelper.getCurrentUserName();
            filters.add(Filter.parse("responsable: '" + userName + "' or usuarisAmbPermis.user:'" + userName + "'"));
            //String[] roles = authenticationHelper.getCurrentUserRoles();
            filters.add(
                    FilterBuilder.or(
                            FilterBuilder.equal("responsable", userName),
                            FilterBuilder.exists(
                                    FilterBuilder.in("usuarisAmbPermis", Filter.parse("[" + userName + "]"))),
                            FilterBuilder.exists(
                                    FilterBuilder.in("grupsAmbPermis", roles))
                    )
            );
        //}
        List<Filter> result = filters.stream().
                filter(f -> f != null && !String.valueOf(f).isEmpty()).
                collect(Collectors.toList());
        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }*/

    @Override
    protected Specification<TascaEntity> additionalSpecification(String[] namedQueries) {
        String userName = authenticationHelper.getCurrentUserName();
        String[] roles = authenticationHelper.getCurrentUserRoles();
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
