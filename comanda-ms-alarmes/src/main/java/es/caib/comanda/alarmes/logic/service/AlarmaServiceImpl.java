package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.helper.AlarmaComprovacioHelper;
import es.caib.comanda.alarmes.logic.helper.AlarmaMailHelper;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaService;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaConfigRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
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
	private final AlarmaMailHelper alarmaMailHelper;
	private final AuthenticationHelper authenticationHelper;

	@Override
	@Transactional
	public void comprovacioScheduledTask() {
		log.debug("Iniciant comprovació d'alarmes...");
		long activadesCount = alarmaConfigRepository.findAll().stream()
				.filter(alarmaComprovacioHelper::comprovar)
				.count();
		log.debug("...comprovació d'alarmes finalitzada ({} alarmes activades)", activadesCount);
	}

	@Override
	@Transactional
	public void enviamentsAgrupatsScheduledTask() {
		log.debug("Iniciant enviaments agrupats d'alarmes...");
		long mailCount = alarmaMailHelper.sendAlarmesAgrupades();
		log.debug("...enviaments agrupats d'alarmes finalitzat ({} correus enviats)", mailCount);
	}

	@Override
	protected String additionalSpringFilter(
			String currentSpringFilter,
			String[] namedQueries) {
		String currentUser = authenticationHelper.getCurrentUserName();
		boolean isAdmin = authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
		if (!isAdmin) {
			return "alarmaConfig.admin:false and alarmaConfig.createdBy:'" + currentUser + "'";
		} else {
			return "alarmaConfig.admin:true or (alarmaConfig.admin:false and alarmaConfig.createdBy:'" + currentUser + "')";
		}
	}

}
