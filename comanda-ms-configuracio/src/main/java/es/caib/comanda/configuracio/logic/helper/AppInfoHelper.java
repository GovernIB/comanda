package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.AppContextEntity;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppManualEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
import es.caib.comanda.configuracio.persist.repository.AppIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ContextRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ManualRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.salut.model.AppInfo;
import es.caib.comanda.ms.salut.model.ContextInfo;
import es.caib.comanda.ms.salut.model.IntegracioInfo;
import es.caib.comanda.ms.salut.model.Manual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Lògica comuna per a consultar la informació de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AppInfoHelper {

	private final EntornAppRepository entornAppRepository;
	private final AppIntegracioRepository appIntegracioRepository;
	private final IntegracioRepository integracioRepository;
	private final SubsistemaRepository subsistemaRepository;
	private final ContextRepository contextRepository;
	private final ManualRepository manualRepository;

	@Lazy
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
	private final SalutServiceClient salutServiceClient;
	private final EstadisticaServiceClient estadisticaServiceClient;
	private final MonitorServiceClient monitorServiceClient;

	@Lazy
	private final RestTemplate restTemplate;

	@Transactional
	public void refreshAppInfo(Long entornAppId) {
		log.debug("Refrescant informació de l'entornApp {}", entornAppId);
		EntornAppEntity entornApp = entornAppRepository.findById(entornAppId)
				.orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, entornAppId.toString()));
		fetchAndStoreAppInfo(entornApp);
	}

	@Transactional
	public void refreshAppInfo() {
		List<EntornAppEntity> entornAppEntities = entornAppRepository.findByActivaTrueAndAppActivaTrue();
		entornAppEntities.forEach(entornApp -> {
			try {
				fetchAndStoreAppInfo(entornApp);
			} catch (Exception ex) {
				log.warn("No s'ha pogut actualitzar info per entorn {}: {}", entornApp.getId(), ex.getMessage());
			}
		});
	}

	public void programarTasquesSalutEstadistica(EntornAppEntity entity) {
		es.caib.comanda.client.model.EntornApp clientEntornApp = toClientEntornApp(entity);
		try {
			salutServiceClient.programar(clientEntornApp, httpAuthorizationHeaderHelper.getAuthorizationHeader());
		} catch (Exception e) {
			log.error("Error al programar l'actualització d'informació de salut per l'entornApp {}", entity.getId(), e);
		}
		try {
			estadisticaServiceClient.programar(clientEntornApp, httpAuthorizationHeaderHelper.getAuthorizationHeader());
		} catch (Exception e) {
			log.error("Error al programar l'actualització d'informació estadística per l'entornApp {}", entity.getId(), e);
		}
	}

	private es.caib.comanda.client.model.EntornApp toClientEntornApp(EntornAppEntity entity) {
		return es.caib.comanda.client.model.EntornApp.builder()
				.id(entity.getId())
				.entorn(EntornRef.builder().id(entity.getEntorn().getId()).nom(entity.getEntorn().getNom()).build())
				.app(AppRef.builder().id(entity.getApp().getId()).nom(entity.getApp().getNom()).build())
				.infoUrl(entity.getInfoUrl())
				.salutUrl(entity.getSalutUrl())
				.estadisticaUrl(entity.getEstadisticaUrl())
				.estadisticaUrl(entity.getEstadisticaUrl())
				.estadisticaCron(entity.getEstadisticaCron())
				.activa(entity.isActiva())
                .compactable(entity.getCompactable())
                .eliminacioMesos(entity.getEliminacioMesos())
                .compactacioMensualMesos(entity.getCompactacioMensualMesos())
				.build();
	}

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
	 *
	 * @param entornApp L'entorn-aplicació per al qual s'ha d'actualitzar la informació.
	 */
	private void fetchAndStoreAppInfo(EntornAppEntity entornApp) {
//		RestTemplate restTemplate = new RestTemplate();
		MonitorApp monitorApp = new MonitorApp(
			entornApp.getId(),
			entornApp.getInfoUrl(),
			monitorServiceClient,
			httpAuthorizationHeaderHelper.getAuthorizationHeader());

		try {
			// Obtenim informació de l'app
			monitorApp.startAction();
			AppInfo appInfo = restTemplate.getForObject(entornApp.getInfoUrl(), AppInfo.class);
			monitorApp.endAction();
			// Guardar la informació de l'app a la base de dades
			if (appInfo != null) {
				entornApp.setVersio(appInfo.getVersio());
				entornApp.setInfoData(appInfo.getData().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
				entornApp.setRevisio(appInfo.getRevisio());
				entornApp.setJdkVersion(appInfo.getJdkVersion());
				refreshIntegracions(entornApp, appInfo.getIntegracions());
				refreshSubsistemes(entornApp, appInfo.getSubsistemes());
				refreshContexts(entornApp, appInfo.getContexts());
			}
		} catch (RestClientException ex) {
			log.warn("No s'ha pogut obtenir informació de salut de l'app {}, entorn {}: {}",
				entornApp.getApp().getNom(),
				entornApp.getEntorn().getNom(),
				ex.getLocalizedMessage());
			if (!monitorApp.isFinishedAction()) {
				monitorApp.endAction(ex);
			}
		}
	}

	private void refreshIntegracions(EntornAppEntity entornApp, List<IntegracioInfo> integracioInfos) {
		List<AppIntegracioEntity> appIntegracionsDb = appIntegracioRepository.findByEntornApp(entornApp);
		List<IntegracioEntity> integracionsDb = integracioRepository.findAll();
		// Actualitzam les integracions existents i cream les integracions que falten a la base de dades
		if (integracioInfos != null) {
			integracioInfos.forEach(iin -> {
				Optional<AppIntegracioEntity> appIntegracioDb = appIntegracionsDb.stream().
						filter(idb -> idb.getIntegracio().getCodi().equals(iin.getCodi())).
						findFirst();
				if (appIntegracioDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació de la integració {}", iin.getCodi());
					appIntegracioDb.get().getIntegracio().setNom(iin.getNom());
					appIntegracioDb.get().setActiva(true);
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nova integració {}", iin.getCodi());
					AppIntegracioEntity integracioNova = new AppIntegracioEntity();
					Optional<IntegracioEntity> integracioDb = integracionsDb.stream()
							.filter(idb -> idb.getCodi().equals(iin.getCodi()))
							.findFirst();
					if (integracioDb.isPresent()) {
						integracioNova.setIntegracio(integracioDb.get());
					} else {
						IntegracioEntity integracioNou = new IntegracioEntity();
						integracioNou.setCodi(iin.getCodi());
						integracioNou.setNom(iin.getNom());
						integracioRepository.save(integracioNou);
						integracioNova.setIntegracio(integracioNou);
					}
					integracioNova.setActiva(true);
					integracioNova.setEntornApp(entornApp);
					appIntegracioRepository.save(integracioNova);
				}
			});
		}
		// Desactivam les integracions que no apareixen a la resposta
		appIntegracionsDb.forEach(idb -> {
			Optional<IntegracioInfo> integracioInfo = integracioInfos != null ? integracioInfos.stream().
					filter(iin -> idb.getIntegracio().getCodi().equals(iin.getCodi())).
					findFirst() : Optional.empty();
			if (integracioInfo.isEmpty()) {
				log.debug("\tDesactivant integració {}", idb.getIntegracio().getCodi());
				idb.setActiva(false);
			}
		});
	}

	private void refreshSubsistemes(EntornAppEntity entornApp, List<AppInfo> subsistemaInfos) {
		List<AppSubsistemaEntity> subsistemesDb = subsistemaRepository.findByEntornApp(entornApp);
		// Actualitzam els subsistemes existents i cream els subsistemes que falten a la base de dades
		if (subsistemaInfos != null) {
			subsistemaInfos.forEach(sin -> {
				Optional<AppSubsistemaEntity> subsistemaDb = subsistemesDb.stream().
						filter(sdb -> sdb.getCodi().equals(sin.getCodi())).
						findFirst();
				if (subsistemaDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació del subsistema {}", sin.getCodi());
					subsistemaDb.get().setNom(sin.getNom());
					subsistemaDb.get().setActiu(true);
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nou subsistema {}", sin.getCodi());
					AppSubsistemaEntity subsistemaNou = new AppSubsistemaEntity();
					subsistemaNou.setCodi(sin.getCodi());
					subsistemaNou.setNom(sin.getNom());
					subsistemaNou.setActiu(true);
					subsistemaNou.setEntornApp(entornApp);
					subsistemaRepository.save(subsistemaNou);
				}
			});
		}
		// Desactivam els subsistemes que no apareixen a la resposta
		subsistemesDb.forEach(sdb -> {
			Optional<AppInfo> subsistemaInfo = subsistemaInfos != null ? subsistemaInfos.stream().
					filter(sin -> sdb.getCodi().equals(sin.getCodi())).
					findFirst() : Optional.empty();
			if (subsistemaInfo.isEmpty()) {
				log.debug("\tDesactivant subsistema {}", sdb.getCodi());
				sdb.setActiu(false);
			}
		});
	}

	private void refreshContexts(EntornAppEntity entornApp, List<ContextInfo> contextInfos) {
		List<AppContextEntity> contextsDb = contextRepository.findByEntornApp(entornApp);
		// Actualitzam els contexts existents i cream els contexts que falten a la base de dades
		if (contextInfos != null) {
			contextInfos.forEach(cin -> {
				Optional<AppContextEntity> contextDb = contextsDb.stream().
						filter(ctx -> ctx.getCodi().equals(cin.getCodi())).
						findFirst();
				if (contextDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació del context {}", cin.getCodi());
					contextDb.get().setNom(cin.getNom());
					contextDb.get().setPath(cin.getPath());
					contextDb.get().setApi(cin.getApi());
					contextDb.get().setActiu(true);
					refreshManuals(contextDb.get(), cin.getManuals());
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nou context {}", cin.getCodi());
					AppContextEntity contextNou = new AppContextEntity();
					contextNou.setCodi(cin.getCodi());
					contextNou.setNom(cin.getNom());
					contextNou.setPath(cin.getPath());
					contextNou.setApi(cin.getApi());
					contextNou.setEntornApp(entornApp);
					contextNou.setActiu(true);
					contextNou = contextRepository.save(contextNou);
					refreshManuals(contextNou, cin.getManuals());
				}
			});
		}
		// Desactivam els contexts que no apareixen a la resposta
		contextsDb.forEach(cdb -> {
			Optional<ContextInfo> contextInfo = contextInfos != null ? contextInfos.stream().
					filter(sin -> cdb.getCodi().equals(sin.getCodi())).
					findFirst() : Optional.empty();
			if (contextInfo.isEmpty()) {
				log.debug("\tDesactivant context {}", cdb.getCodi());
				cdb.setActiu(false);
			}
		});
	}

	private void refreshManuals(AppContextEntity appContext, List<Manual> manuals) {
		List<AppManualEntity> manualsDb = manualRepository.findByAppContext(appContext);
		// Actualitzam els manuals existents i cream els manuals que falten a la base de dades
		if (manuals != null) {
			manuals.forEach(m -> {
				Optional<AppManualEntity> manualDb = manualsDb.stream().
						filter(am -> am.getNom().equals(m.getNom())).
						findFirst();
				if (manualDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació del manual {}", m.getNom());
					manualDb.get().setPath(m.getPath());
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nou manual {}", m.getNom());
					AppManualEntity manualNou = new AppManualEntity();
					manualNou.setNom(m.getNom());
					manualNou.setPath(m.getPath());
					manualNou.setAppContext(appContext);
					manualRepository.save(manualNou);
				}
			});
		}
		// Eliminam els manuals que no apareixen a la resposta
		manualsDb.forEach(m -> {
			Optional<Manual> manual = manuals != null ? manuals.stream().
					filter(min -> m.getNom().equals(min.getNom())).
					findFirst() : Optional.empty();
			if (manual.isEmpty()) {
				log.debug("\tEliminant manual {}", m.getNom());
				appContext.getManuals().remove(m);
				manualRepository.delete(m);
			}
		});
	}

}
