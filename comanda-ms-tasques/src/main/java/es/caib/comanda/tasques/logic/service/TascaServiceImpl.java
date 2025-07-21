package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.logic.intf.service.TascaService;
import es.caib.comanda.tasques.persist.entity.TascaEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import static es.caib.comanda.ms.broker.model.Cues.CUA_TASQUES;

@Slf4j
@Service
public class TascaServiceImpl extends BaseMutableResourceService<Tasca, Long, TascaEntity> implements TascaService {

    @JmsListener(destination = CUA_TASQUES)
    public void receiveMessage(es.caib.comanda.ms.broker.model.Tasca tasca) {
        log.debug("Tasca rebuda: " + tasca);

        // TODO: Desar tasca a BBDD
    }

}
