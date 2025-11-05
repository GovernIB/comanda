package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.logic.intf.service.AvisService;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.avisos.persist.repository.AvisRepository;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static es.caib.comanda.ms.broker.model.Cues.CUA_AVISOS;
import static es.caib.comanda.ms.broker.model.Cues.CUA_TASQUES;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvisServiceImpl extends BaseMutableResourceService<Avis, Long, AvisEntity> implements AvisService {

    private final AvisClientHelper avisClientHelper;

    @PostConstruct
    public void init() {
        register(Avis.PERSPECTIVE_PATH, new PathPerspectiveApplicator());
    }

    @JmsListener(destination = CUA_AVISOS)
    public void receiveMessage(es.caib.comanda.ms.broker.model.Avis avisBroker) {

        log.debug("Processat avís de la cua " + CUA_TASQUES + " (avís={})", avisBroker);
        var entornApp = avisClientHelper.entornAppFindByEntornCodiAndAppCodi(avisBroker.getEntornCodi(), avisBroker.getAppCodi());
        if (entornApp.isEmpty()) {
            throw new ResourceNotFoundException(EntornApp.class, "(entornCodi=" + avisBroker.getEntornCodi() + ", appCodi=" + avisBroker.getAppCodi() + ")");
        }
        var avisExistent = ((AvisRepository)entityRepository).findByEntornAppIdAndIdentificador(entornApp.get().getId(), avisBroker.getIdentificador());
        if (avisExistent.isEmpty()) {
            var avis = new Avis();
            avis.setEntornAppId(entornApp.get().getId());
            avis.setEntornId(entornApp.get().getEntorn().getId());
            avis.setAppId(entornApp.get().getApp().getId());
            avis.setIdentificador(avisBroker.getIdentificador());
            avis.setTipus(avisBroker.getTipus());
            avis.setNom(avisBroker.getNom());
            avis.setDescripcio(avisBroker.getDescripcio());
            avis.setDataInici(convertToLocalDateTime(avisBroker.getDataInici()));
            avis.setDataFi(convertToLocalDateTime(avisBroker.getDataFi()));
            entityRepository.save(AvisEntity.builder().avis(avis).build());
        } else {
            var avis = avisExistent.get();
            avis.setTipus(avisBroker.getTipus());
            avis.setNom(avisBroker.getNom());
            avis.setDescripcio(avisBroker.getDescripcio());
            avis.setDataInici(convertToLocalDateTime(avisBroker.getDataInici()));
            avis.setDataFi(convertToLocalDateTime(avisBroker.getDataFi()));
            entityRepository.save(avis);
        }
    }

    private static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        return dateToConvert != null ? dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime() : null;
    }

    public class PathPerspectiveApplicator implements PerspectiveApplicator<AvisEntity, Avis> {
        @Override
        public void applySingle(String code, AvisEntity entity, Avis resource) throws PerspectiveApplicationException {
            EntornApp entornApp = avisClientHelper.entornAppFindById(entity.getEntornAppId());
            resource.setTreePath(new String[]{entornApp.getApp().getNom(), entornApp.getEntorn().getNom(), resource.getIdentificador()});
        }
    }

}
