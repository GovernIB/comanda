package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.helper.AlarmaComprovacioHelper;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementació del servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmaServiceImpl extends BaseMutableResourceService<Alarma, Long, AlarmaEntity> implements AlarmaService {

	private final AlarmaComprovacioHelper alarmaComprovacioHelper;
	private final AlarmaConfigRepository alarmaConfigRepository;

	@Override
	@Transactional
	public void comprovacioScheduledTask() {
		log.info("Iniciant comprovació d'alarmes...");
		long activadesCount = alarmaConfigRepository.findAll().stream()
				.filter(alarmaComprovacioHelper::comprovar)
				.count();
		log.info("...comprovació d'alarmes finalitzada ({} alarmes activades)", activadesCount);
	}

}
