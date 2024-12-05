package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.helper.SalutInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementació del servei de gestió d'aplicacions.
 *
 * @author Limit Tecnologies
 */
@Slf4j
@Service
public class AppServiceImpl extends BaseMutableResourceService<App, Long, AppEntity> implements AppService {

	@Autowired
	private AppInfoHelper appInfoHelper;
	@Autowired
	private SalutInfoHelper salutInfoHelper;
	@Autowired
	private AppRepository appRepository;

	@Override
	@Transactional
	public void refreshAppInfo() {
		log.debug("Iniciant refresc periòdic de la informació de les apps");
		List<AppEntity> apps = appRepository.findByActivaTrue();
		apps.forEach(a -> {
			try {
				appInfoHelper.refreshAppInfo(a);
			} catch (Exception ex) {
				log.error("No s'ha pogut refrescar la informació de l'aplicació " + a.getCodi(), ex);
			}
		});
	}

	@Override
	@Transactional
	public void getSalutInfo() {
		log.debug("Iniciant consulta periòdica de la informació de salut");
		List<AppEntity> apps = appRepository.findByActivaTrue();
		apps.forEach(a -> {
			try {
				salutInfoHelper.getSalutInfo(a);
			} catch (Exception ex) {
				log.error("No s'ha pogut refrescar la informació de l'aplicació " + a.getCodi(), ex);
			}
		});
	}

}
