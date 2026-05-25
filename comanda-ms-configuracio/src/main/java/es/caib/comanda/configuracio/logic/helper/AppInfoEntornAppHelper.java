package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppInfoEntornAppHelper {
	private final EntornAppRepository entornAppRepository;

	/**
	 * Actualitza la informació de l'aplicació associada a un entorn concret.
	 * <p>
	 * Aquesta actualització es realitza mitjançant una crida HTTP al servei monitoritzat
	 * de l'aplicació, obtenint la seva versió, data de desplegament i integracions/subsistemes.
	 * La informació obtinguda es desa a la base de dades per mantenir actualitzat l'estat
	 * de les aplicacions en cada entorn.
	 * </p>
	 * <p>
	 * En cas d'error en la comunicació, es registra un avís i es continua l'execució sense interrompre el procés global.
	 * </p>
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void storeAppInfo(AppInfo appInfo, Long entornAppId) {
		EntornAppEntity entornAppEntity = entornAppRepository.findById(entornAppId)
				.orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, entornAppId.toString()));
		if (appInfo != null) {
			entornAppEntity.setVersio(appInfo.getVersio());
			entornAppEntity.setInfoData(appInfo.getData().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			entornAppEntity.setRevisio(appInfo.getRevisio());
			entornAppEntity.setJdkVersion(appInfo.getJdkVersion());
		}
	}
}
