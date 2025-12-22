package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.TemplateEstils;
import es.caib.comanda.estadistica.logic.intf.service.TemplateEstilsService;
import es.caib.comanda.estadistica.persist.entity.dashboard.TemplateEstilsEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb les plantilles d'estils.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TemplateEstilsServiceImpl extends BaseMutableResourceService<TemplateEstils, Long, TemplateEstilsEntity> implements TemplateEstilsService {

}
