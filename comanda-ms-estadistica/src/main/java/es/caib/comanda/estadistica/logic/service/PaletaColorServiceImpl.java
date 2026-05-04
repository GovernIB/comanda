package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.logic.intf.service.PaletaColorService;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaletaColorServiceImpl extends BaseMutableResourceService<PaletaColor, Long, PaletaColorEntity> implements PaletaColorService {
}
