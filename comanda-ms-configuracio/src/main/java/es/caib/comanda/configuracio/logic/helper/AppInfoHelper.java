package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.salut.model.AppInfo;
import es.caib.comanda.ms.salut.model.IntegracioInfo;
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
	private final IntegracioRepository integracioRepository;
	private final SubsistemaRepository subsistemaRepository;

	@Lazy
	private final KeycloakHelper keycloakHelper;
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
			salutServiceClient.programar(clientEntornApp, keycloakHelper.getAuthorizationHeader());
		} catch (Exception e) {
			log.error("Error al programar l'actualització d'informació de salut per l'entornApp {}", entity.getId(), e);
		}
		try {
			estadisticaServiceClient.programar(clientEntornApp, keycloakHelper.getAuthorizationHeader());
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
				.infoInterval(entity.getInfoInterval())
				.salutUrl(entity.getSalutUrl())
				.salutInterval(entity.getSalutInterval())
				.estadisticaUrl(entity.getEstadisticaUrl())
				.estadisticaUrl(entity.getEstadisticaUrl())
				.estadisticaCron(entity.getEstadisticaCron())
				.activa(entity.isActiva())
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
			keycloakHelper.getAuthorizationHeader());

		try {
			// Obtenim informació de l'app
			monitorApp.startAction();
			AppInfo appInfo = restTemplate.getForObject(entornApp.getInfoUrl(), AppInfo.class);
			monitorApp.endAction();
			// Guardar la informació de l'app a la base de dades
			if (appInfo != null) {
				entornApp.setInfoData(
					appInfo.getData().toInstant().
						atZone(ZoneId.systemDefault()).
						toLocalDateTime());
				entornApp.setVersio(appInfo.getVersio());
				refreshIntegracions(entornApp, appInfo.getIntegracions());
				refreshSubsistemes(entornApp, appInfo.getSubsistemes());
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
		List<AppIntegracioEntity> integracionsDb = integracioRepository.findByEntornApp(entornApp);
		// Actualitzam les integracions existents i cream les integracions que falten a la base de dades
		if (integracioInfos != null) {
			integracioInfos.forEach(iin -> {
				Optional<AppIntegracioEntity> integracioDb = integracionsDb.stream().
						filter(idb -> idb.getCodi().equals(iin.getCodi())).
						findFirst();
				if (integracioDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació de la integració {}", iin.getCodi());
					integracioDb.get().setNom(iin.getNom());
					integracioDb.get().setActiva(true);
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nova integració {}", iin.getCodi());
					AppIntegracioEntity integracioNova = new AppIntegracioEntity();
					integracioNova.setCodi(iin.getCodi());
					integracioNova.setNom(iin.getNom());
					integracioNova.setActiva(true);
					integracioNova.setEntornApp(entornApp);
					integracioRepository.save(integracioNova);
				}
			});
		}
		// Desactivam les integracions que no apareixen a la resposta
		integracionsDb.forEach(idb -> {
			Optional<IntegracioInfo> integracioInfo = integracioInfos != null ? integracioInfos.stream().
					filter(iin -> idb.getCodi().equals(iin.getCodi())).
					findFirst() : Optional.empty();
			if (integracioInfo.isEmpty()) {
				log.debug("\tDesactivant integració {}", idb.getCodi());
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

}
