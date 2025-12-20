package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.logic.intf.service.AvisService;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static es.caib.comanda.base.config.Cues.CUA_AVISOS;
import static es.caib.comanda.base.config.Cues.CUA_TASQUES;


@Slf4j
@Service
@RequiredArgsConstructor
public class AvisServiceImpl extends BaseMutableResourceService<Avis, Long, AvisEntity> implements AvisService {

    private final AuthenticationHelper authenticationHelper;
    private final AvisClientHelper avisClientHelper;

    @PostConstruct
    public void init() {
        register(Avis.PERSPECTIVE_PATH, new PathPerspectiveApplicator());
    }

    @JmsListener(destination = CUA_AVISOS)
    public void receiveMessage(@Payload es.caib.comanda.model.v1.avis.Avis avisBroker,
                               Message message) throws JMSException {
        message.acknowledge();
        log.debug("Processat avís de la cua " + CUA_TASQUES + " (avís={})", avisBroker);
        Optional<EntornApp> entornApp = avisClientHelper.entornAppFindByEntornCodiAndAppCodi(
                avisBroker.getEntornCodi(),
                avisBroker.getAppCodi());
        if (entornApp.isEmpty()) {
            throw new ResourceNotFoundException(
                    EntornApp.class,
                    "(entornCodi=" + avisBroker.getEntornCodi() + ", appCodi=" + avisBroker.getAppCodi() + ")");
        }
        Optional<AvisEntity> avisExistent = ((AvisRepository)entityRepository).findByEntornAppIdAndIdentificador(
                entornApp.get().getId(),
                avisBroker.getIdentificador());
        if (avisExistent.isEmpty()) {
            Avis avis = new Avis();
            avis.setEntornAppId(entornApp.get().getId());
            avis.setEntornId(entornApp.get().getEntorn().getId());
            avis.setAppId(entornApp.get().getApp().getId());
            avis.setIdentificador(avisBroker.getIdentificador());
            avis.setTipus(avisBroker.getTipus());
            avis.setNom(avisBroker.getNom());
            avis.setDescripcio(avisBroker.getDescripcio());
            avis.setDataInici(avisBroker.getDataInici() != null ? avisBroker.getDataInici().toLocalDateTime() : null);
            avis.setDataFi(avisBroker.getDataFi() != null ? avisBroker.getDataFi().toLocalDateTime() : null);
            avis.setUrl(avisBroker.getRedireccio());
            avis.setResponsable(avisBroker.getResponsable());
            avis.setGrup(avisBroker.getGrup());
            avis.setUsuarisAmbPermis(avisBroker.getUsuarisAmbPermis());
            avis.setGrupsAmbPermis(avisBroker.getGrupsAmbPermis());
            entityRepository.save(AvisEntity.builder().avis(avis).build());
        } else {
            AvisEntity avis = avisExistent.get();
            avis.setTipus(avisBroker.getTipus());
            avis.setNom(avisBroker.getNom());
            avis.setDescripcio(avisBroker.getDescripcio());
            avis.setDataInici(avisBroker.getDataInici() != null ? avisBroker.getDataInici().toLocalDateTime() : null);
            avis.setDataFi(avisBroker.getDataFi() != null ? avisBroker.getDataFi().toLocalDateTime() : null);
            avis.setUrl(avisBroker.getRedireccio());
            avis.setResponsable(avisBroker.getResponsable());
            avis.setGrup(avisBroker.getGrup());
            avis.setUsuarisAmbPermis(avisBroker.getUsuarisAmbPermis());
            avis.setGrupsAmbPermis(avisBroker.getGrupsAmbPermis());
            entityRepository.save(avis);
        }
    }

    private static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert != null ? dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime() : null;
    }

    @Override
    protected Specification<AvisEntity> additionalSpecification(String[] namedQueries) {
        String userName = authenticationHelper.getCurrentUserName();
        String[] roles = authenticationHelper.getCurrentUserRoles();
        return teGrupSiNoNull(roles).and(
                teResponsable(userName).
                        or(tePermisUsuari(userName)).
                        or(tePermisGrupIn(roles)).
                        or(avisSensePermisos()));
    }

    public static Specification<AvisEntity> teGrupSiNoNull(String[] grups) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("grup")),
                root.get("grup").in(Arrays.asList(grups))
        );
    }
    private Specification<AvisEntity> teResponsable(String responsable) {
        return (root, query, cb) -> cb.equal(root.get("responsable"), responsable);
    }
    private Specification<AvisEntity> tePermisUsuari(String usuari) {
        return (root, query, cb) -> {
            Join<AvisEntity, String> join = root.join("usuarisAmbPermis", JoinType.LEFT);
            query.distinct(true);
            return cb.equal(join, usuari);
        };
    }
    private Specification<AvisEntity> tePermisGrupIn(String[] grups) {
        return (root, query, cb) -> {
            Join<AvisEntity, String> join = root.join("grupsAmbPermis", JoinType.LEFT);
            query.distinct(true);
            return join.in(Arrays.asList(grups));
        };
    }
    private Specification<AvisEntity> avisSensePermisos() {
        return (root, query, cb) -> cb.and(
                cb.isEmpty(root.get("usuarisAmbPermis")),
                cb.isEmpty(root.get("grupsAmbPermis"))
        );
    }

    public class PathPerspectiveApplicator implements PerspectiveApplicator<AvisEntity, Avis> {
        @Override
        public void applySingle(String code, AvisEntity entity, Avis resource) throws PerspectiveApplicationException {
            EntornApp entornApp = avisClientHelper.entornAppFindById(entity.getEntornAppId());
            resource.setTreePath(new String[]{entornApp.getApp().getNom(), entornApp.getEntorn().getNom(), resource.getIdentificador()});
        }
    }

}
