package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
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
 * @author Límit Tecnologies
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
		apps.forEach(app -> {
			if (app.getEntornApps().isEmpty())
				return;

			app.getEntornApps().parallelStream().forEach(entornApp -> {
				try {
					if (entornApp.isActiva()) {
						appInfoHelper.refreshAppInfo(entornApp);
					}
				} catch (Exception ex) {
					log.error("No s'ha pogut refrescar la informació de l'aplicació {} en l'entorn {}",
							app.getCodi(),
							entornApp.getEntorn().getCodi(),
							ex);
				}
			});
		});
	}

	@Override
	protected void afterConversion(AppEntity entity, App resource) {
		List<EntornAppEntity> entornApps = entity.getEntornApps();
		if (!entornApps.isEmpty()) {
			resource.setEntornApps(
					entornApps.stream().map(e -> {
						EntornApp entornApp = EntornApp.builder()
								.app(ResourceReference.toResourceReference(entity.getId(), entity.getNom()))
								.entorn(ResourceReference.toResourceReference(e.getEntorn().getId(), e.getEntorn().getNom()))
								.infoUrl(e.getInfoUrl())
								.infoData(e.getInfoData())
								.versio(e.getVersio())
								.activa(e.isActiva())
								.salutUrl(e.getSalutUrl())
								.salutInterval(e.getSalutInterval())
								.estadisticaInfoUrl(e.getEstadisticaInfoUrl())
								.estadisticaUrl(e.getEstadisticaUrl())
								.estadisticaCron(e.getEstadisticaCron())
								.build();
						entornApp.setId(e.getId());
						return entornApp;
					}).collect(Collectors.toList())
			);
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
