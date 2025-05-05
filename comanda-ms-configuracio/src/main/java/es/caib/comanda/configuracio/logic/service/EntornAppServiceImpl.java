package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.service.EntornAppService;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementació del servei de gestió d'aplicacions per entorn.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EntornAppServiceImpl extends BaseMutableResourceService<EntornApp, Long, EntornAppEntity> implements EntornAppService {

    @Autowired
    private IntegracioRepository integracioRepository;
    @Autowired
    private SubsistemaRepository subsistemaRepository;

    @Override
    protected void afterConversion(EntornAppEntity entity, EntornApp resource) {
        List<AppIntegracioEntity> integracions = integracioRepository.findByEntornApp(entity);
        if (!integracions.isEmpty()) {
            resource.setIntegracions(
                    integracions.stream().map(i -> new AppIntegracio(
                            i.getCodi(),
                            i.getNom(),
                            i.isActiva(),
                            null)).collect(Collectors.toList()));
        }
        List<AppSubsistemaEntity> subsistemes = subsistemaRepository.findByEntornApp(entity);
        if (!integracions.isEmpty()) {
            resource.setSubsistemes(
                    subsistemes.stream().map(s -> new AppSubsistema(
                            s.getCodi(),
                            s.getNom(),
                            s.isActiu(),
                            null)).collect(Collectors.toList()));
        }
    }

}
