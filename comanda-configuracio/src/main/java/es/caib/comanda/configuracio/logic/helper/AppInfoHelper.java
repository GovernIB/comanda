package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.SubsistemaEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.salut.model.AppInfo;
import es.caib.comanda.salut.model.IntegracioInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Lògica comuna per a consultar la informació de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
public class AppInfoHelper {

	@Autowired
	private AppRepository appRepository;
	@Autowired
	private IntegracioRepository integracioRepository;
	@Autowired
	private SubsistemaRepository subsistemaRepository;

	public void refreshAppInfo(AppEntity app) {
		log.debug("Refrescant informació de l'app {}", app.getCodi());
		RestTemplate restTemplate = new RestTemplate();
		try {
			AppInfo appInfo = restTemplate.getForObject(app.getInfoUrl(), AppInfo.class);
			if (appInfo != null) {
				refreshIntegracions(app, appInfo.getIntegracions());
				refreshSubsistemes(app, appInfo.getSubsistemes());
			}
		} catch (RestClientException ex) {
			log.warn("No s'ha pogut obtenir informació de salut de l'app {}: {}",
					app.getCodi(),
					ex.getLocalizedMessage());
		}
	}

	private void refreshIntegracions(AppEntity app, List<IntegracioInfo> integracioInfos) {
		List<IntegracioEntity> integracionsDb = integracioRepository.findByApp(app);
		// Actualitzam les integracions existents i cream les integracions que falten a la base de dades
		if (integracioInfos != null) {
			integracioInfos.forEach(iin -> {
				Optional<IntegracioEntity> integracioDb = integracionsDb.stream().
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
					IntegracioEntity integracioNova = new IntegracioEntity();
					integracioNova.setCodi(iin.getCodi());
					integracioNova.setNom(iin.getNom());
					integracioNova.setActiva(true);
					integracioNova.setApp(app);
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

	private void refreshSubsistemes(AppEntity app, List<AppInfo> subsistemaInfos) {
		List<SubsistemaEntity> subsistemesDb = subsistemaRepository.findByApp(app);
		// Actualitzam els subsistemes existents i cream els subsistemes que falten a la base de dades
		if (subsistemaInfos != null) {
			subsistemaInfos.forEach(sin -> {
				Optional<SubsistemaEntity> subsistemaDb = subsistemesDb.stream().
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
					SubsistemaEntity subsistemaNou = new SubsistemaEntity();
					subsistemaNou.setCodi(sin.getCodi());
					subsistemaNou.setNom(sin.getNom());
					subsistemaNou.setActiu(true);
					subsistemaNou.setApp(app);
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
