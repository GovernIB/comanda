package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

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
	private AppRepository appRepository;
	@Autowired
	private IntegracioRepository integracioRepository;
	@Autowired
	private SubsistemaRepository subsistemaRepository;

	@PostConstruct
	public void init() {
		register(new RefreshAction(this));
	}

	@Override
	@Transactional
	public void refreshAppInfo() {
		log.debug("Iniciant refresc periòdic de la informació de les apps");
		List<AppEntity> apps = appRepository.findByActivaTrue();
		apps.forEach(a -> {
			try {
				appInfoHelper.refreshAppInfo(a);
			} catch (Exception ex) {
				log.error("No s'ha pogut refrescar la informació de l'aplicació {}", a.getCodi(), ex);
			}
		});
	}

	@Override
	protected void afterConversion(AppEntity entity, App resource) {
		List<AppIntegracioEntity> integracions = integracioRepository.findByApp(entity);
		if (!integracions.isEmpty()) {
			resource.setIntegracions(
					integracions.stream().map(i -> new AppIntegracio(
							i.getCodi(),
							i.getNom(),
							i.isActiva(),
							null)).collect(Collectors.toList()));
		}
		List<AppSubsistemaEntity> subsistemes = subsistemaRepository.findByApp(entity);
		if (!integracions.isEmpty()) {
			resource.setSubsistemes(
					subsistemes.stream().map(s -> new AppSubsistema(
								s.getCodi(),
								s.getNom(),
								s.isActiu(),
								null)).collect(Collectors.toList()));
		}
	}

	public static class RefreshAction implements ActionExecutor<Object, Object> {
		private final AppServiceImpl appServiceImpl;
		public RefreshAction(AppServiceImpl appServiceImpl) {
			this.appServiceImpl = appServiceImpl;
		}
		@Override
		public String[] getSupportedActionCodes() {
			return new String[] { "refresh" };
		}
		@Override
		public Object exec(String code, Object params) throws ActionExecutionException {
			appServiceImpl.refreshAppInfo();
			return null;
		}
	}

}
