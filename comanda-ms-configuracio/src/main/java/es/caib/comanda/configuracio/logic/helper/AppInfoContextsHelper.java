package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.AppContextEntity;
import es.caib.comanda.configuracio.persist.entity.AppManualEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.ContextRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.ManualRepository;
import es.caib.comanda.model.v1.salut.ContextInfo;
import es.caib.comanda.model.v1.salut.Manual;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static es.caib.comanda.configuracio.logic.helper.AppInfoHelper.truncateString;
import static es.caib.comanda.configuracio.logic.helper.AppInfoHelper.validateObject;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppInfoContextsHelper {
	private final EntornAppRepository entornAppRepository;
	private final ContextRepository contextRepository;
	private final ManualRepository manualRepository;

	private void autoCorrectContextInfo(ContextInfo contextInfo) {
		if (contextInfo.getManuals() == null) {
			return;
		}
		Set<String> uniqueManualNames = new HashSet<>();
		List<Manual> correctedManuals = new ArrayList<>();
		for (Manual manual : contextInfo.getManuals()) {
			String nom = manual.getNom();
			// Ignoram duplicats
			if (uniqueManualNames.add(nom)) {
				correctedManuals.add(manual);
			}
		}
		contextInfo.setManuals(correctedManuals);
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

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refreshContexts(Long entornAppId, List<ContextInfo> contextInfos) {
		EntornAppEntity entornAppEntity = entornAppRepository.findById(entornAppId)
				.orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, entornAppId.toString()));
		List<AppContextEntity> contextsDb = contextRepository.findByEntornApp(entornAppEntity);

		// Filtram els contexts invalids i duplicats
		Set<String> uniqueContextCodis = new HashSet<>();
		var filteredContextInfos = contextInfos != null ? contextInfos.stream()
				.filter(cin -> {
					if (!uniqueContextCodis.add(cin.getCodi())) {
						log.warn("Codi de context duplicat: {} (entornApp: {})", cin.getCodi(), entornAppId);
						return false;
					}
					var violations = validateObject(cin);
					// Intentam corregir errors comuns
					if (!violations.isEmpty()) {
						log.warn("Context {} (entornApp: {}) no validat: {}", cin.getCodi(), entornAppId, violations);
						autoCorrectContextInfo(cin);
						// Revalidam després de corregir
						violations = validateObject(cin);
						log.warn(violations.isEmpty() ? "Context {} (entornApp: {}) s'ha corregit automàticament" : "Context {} (entornApp: {}) no s'ha pogut corregir automàticament: {}", cin.getCodi(), entornAppId, violations);
					}
					return violations.isEmpty();
				})
				.collect(Collectors.toList()) : null;

		// Actualitzam els contexts existents i cream els contexts que falten a la base de dades
		if (filteredContextInfos != null) {
			filteredContextInfos.forEach(cin -> {
				Optional<AppContextEntity> contextDb = contextsDb.stream().
						filter(ctx -> ctx.getCodi().equals(cin.getCodi())).
						findFirst();
				if (contextDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació del context {}", cin.getCodi());
					contextDb.get().setNom(truncateString(cin.getNom(), 255));
					contextDb.get().setPath(cin.getPath());
					contextDb.get().setApi(cin.getApi());
					contextDb.get().setActiu(true);
					refreshManuals(contextDb.get(), cin.getManuals());
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nou context {}", cin.getCodi());
					AppContextEntity contextNou = new AppContextEntity();
					contextNou.setCodi(cin.getCodi());
					contextNou.setNom(truncateString(cin.getNom(), 255));
					contextNou.setPath(cin.getPath());
					contextNou.setApi(cin.getApi());
					contextNou.setEntornApp(entornAppEntity);
					contextNou.setActiu(true);
					contextNou = contextRepository.save(contextNou);
					refreshManuals(contextNou, cin.getManuals());
				}
			});
		}
		// Desactivam els contexts que no apareixen a la resposta
		contextsDb.forEach(cdb -> {
			Optional<ContextInfo> contextInfo = filteredContextInfos != null ? filteredContextInfos.stream().
					filter(sin -> cdb.getCodi().equals(sin.getCodi())).
					findFirst() : Optional.empty();
			if (contextInfo.isEmpty()) {
				log.debug("\tDesactivant context {}", cdb.getCodi());
				cdb.setActiu(false);
			}
		});
	}

}
