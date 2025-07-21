package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.logic.intf.service.AvisService;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import static es.caib.comanda.ms.broker.model.Cues.CUA_AVISOS;

@Slf4j
@Service
public class AvisServiceImpl extends BaseMutableResourceService<Avis, Long, AvisEntity> implements AvisService {

    @JmsListener(destination = CUA_AVISOS)
    public void receiveMessage(es.caib.comanda.ms.broker.model.Avis avis) {
        log.debug("Avís rebut: " + avis);

        // TODO: Desar avís a BBDD
    }

}
