package es.caib.comanda.permisos.logic.service;

import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import es.caib.comanda.permisos.logic.intf.service.PermisService;
import es.caib.comanda.permisos.persist.entity.PermisEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import static es.caib.comanda.ms.broker.model.Cues.CUA_PERMISOS;

@Slf4j
@Service
public class PermisServiceImpl extends BaseMutableResourceService<Permis, Long, PermisEntity> implements PermisService {

    @JmsListener(destination = CUA_PERMISOS)
    public void receiveMessage(es.caib.comanda.ms.broker.model.Permis permis) {
        log.debug("Permís rebut: " + permis);

        // TODO: Desar permis a BBDD
    }

}
