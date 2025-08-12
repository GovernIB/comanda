package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.logic.intf.service.AvisService;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static es.caib.comanda.ms.broker.model.Cues.CUA_AVISOS;

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
    public void receiveMessage(es.caib.comanda.ms.broker.model.Avis avis) {
        log.debug("Avís rebut: " + avis);

        // TODO: Desar avís a BBDD
    }

    public class PathPerspectiveApplicator implements PerspectiveApplicator<AvisEntity, Avis> {
        @Override
        public void applySingle(String code, AvisEntity entity, Avis resource) throws PerspectiveApplicationException {
            EntornApp entornApp = avisClientHelper.entornAppFindById(entity.getEntornAppId());
            resource.setTreePath(new String[]{entornApp.getApp().getNom(), entornApp.getEntorn().getNom(), resource.getIdentificador()});
        }
    }
}
