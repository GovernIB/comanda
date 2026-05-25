package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
import es.caib.comanda.configuracio.persist.repository.AppIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.model.v1.salut.IntegracioInfo;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static es.caib.comanda.configuracio.logic.helper.AppInfoHelper.truncateString;
import static es.caib.comanda.configuracio.logic.helper.AppInfoHelper.validateObject;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppInfoIntegracionsHelper {
	private final EntornAppRepository entornAppRepository;
	private final AppIntegracioRepository appIntegracioRepository;
	private final IntegracioRepository integracioRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refreshIntegracions(Long entornAppId, List<IntegracioInfo> integracioInfos) {
		EntornAppEntity entornAppEntity = entornAppRepository.findById(entornAppId)
				.orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, entornAppId.toString()));
		List<AppIntegracioEntity> appIntegracionsDb = appIntegracioRepository.findByEntornApp(entornAppEntity);
		List<IntegracioEntity> integracionsDb = integracioRepository.findAll();

		// Filtram les integracions invalides i duplicades
		Set<String> uniqueIntegracioCodis = new HashSet<>();
		List<IntegracioInfo> filteredIntegracioInfos = integracioInfos != null ? integracioInfos.stream()
				.filter(iin -> {
					if (!uniqueIntegracioCodis.add(iin.getCodi())) {
						log.warn("Codi d'integració duplicat: {} (entornApp: {})", iin.getCodi(), entornAppId);
						return false;
					}
					var violations = validateObject(iin);
					if (!violations.isEmpty()) {
						log.warn("Integració {} (entornApp: {}) no validada: {}", iin.getCodi(), entornAppId, violations);
						return false;
					}
					return true;
				})
				.collect(Collectors.toList()) : null;

		// Actualitzam les integracions existents i cream les integracions que falten a la base de dades
		if (filteredIntegracioInfos != null) {
			filteredIntegracioInfos.forEach(iin -> {
				Optional<AppIntegracioEntity> appIntegracioDb = appIntegracionsDb.stream().
						filter(idb -> idb.getIntegracio().getCodi().equals(iin.getCodi())).
						findFirst();
				if (appIntegracioDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació de la integració {}", iin.getCodi());
					appIntegracioDb.get().getIntegracio().setNom(truncateString(iin.getNom(), 255));
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
						integracioNou.setNom(truncateString(iin.getNom(), 255));
						integracioRepository.save(integracioNou);
						integracioNova.setIntegracio(integracioNou);
					}
					integracioNova.setActiva(true);
					integracioNova.setEntornApp(entornAppEntity);
					appIntegracioRepository.save(integracioNova);
				}
			});
		}
		// Desactivam les integracions que no apareixen a la resposta
		appIntegracionsDb.forEach(idb -> {
			Optional<IntegracioInfo> integracioInfo = filteredIntegracioInfos != null ? filteredIntegracioInfos.stream().
					filter(iin -> idb.getIntegracio().getCodi().equals(iin.getCodi())).
					findFirst() : Optional.empty();
			if (integracioInfo.isEmpty()) {
				log.debug("\tDesactivant integració {}", idb.getIntegracio().getCodi());
				idb.setActiva(false);
			}
		});
	}

}
