package es.caib.comanda.usuaris.logic.service;

import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.logic.intf.service.UsuariService;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;
import es.caib.comanda.usuaris.persist.repository.UsuariRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UsuariServiceImpl extends BaseMutableResourceService<Usuari, Long, UsuariEntity> implements UsuariService {

    @Override
    public Usuari getOneByCodi(String codi) {
        log.debug("Obtenint usuari {codi}", codi);
        Optional<UsuariEntity> usuariEntity = ((UsuariRepository) entityRepository).findByCodi(codi);
        if (usuariEntity.isPresent()) {
            return entityToResource(usuariEntity.get());
        }
        return null;
    }

}
