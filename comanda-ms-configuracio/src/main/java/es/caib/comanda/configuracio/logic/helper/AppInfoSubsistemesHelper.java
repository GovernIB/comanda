package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.model.v1.salut.SubsistemaInfo;
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
public class AppInfoSubsistemesHelper {
	private final EntornAppRepository entornAppRepository;
	private final SubsistemaRepository subsistemaRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void refreshSubsistemes(Long entornAppId, List<SubsistemaInfo> subsistemaInfos) {
		EntornAppEntity entornAppEntity = entornAppRepository.findById(entornAppId)
				.orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, entornAppId.toString()));
		List<AppSubsistemaEntity> subsistemesDb = subsistemaRepository.findByEntornApp(entornAppEntity);

		// Filtram els subsistemes invalids i duplicats
		Set<String> uniqueSubsistemaCodis = new HashSet<>();
		var filteredSubsistemaInfos = subsistemaInfos != null ? subsistemaInfos.stream()
				.filter(sin -> {
					if (!uniqueSubsistemaCodis.add(sin.getCodi())) {
						log.warn("Codi de subsistema duplicat: {} (entornApp: {})", sin.getCodi(), entornAppId);
						return false;
					}
					var violations = validateObject(sin);
					if (!violations.isEmpty()) {
						log.warn("Subsistema {} (entornApp: {}) no validat: {}", sin.getCodi(), entornAppId, violations);
						return false;
					}
					return true;
				})
				.collect(Collectors.toList()) : null;

		// Actualitzam els subsistemes existents i cream els subsistemes que falten a la base de dades
		if (filteredSubsistemaInfos != null) {
			filteredSubsistemaInfos.forEach(sin -> {
				Optional<AppSubsistemaEntity> subsistemaDb = subsistemesDb.stream().
						filter(sdb -> sdb.getCodi().equals(sin.getCodi())).
						findFirst();
				if (subsistemaDb.isPresent()) {
					// Si la integració ja existeix l'actualitzam
					log.debug("\tActualitzant informació del subsistema {}", sin.getCodi());
					subsistemaDb.get().setNom(truncateString(sin.getNom(), 255));
					subsistemaDb.get().setActiu(true);
				} else {
					// Si la integració no existeix la cream
					log.debug("\tCreant nou subsistema {}", sin.getCodi());
					AppSubsistemaEntity subsistemaNou = new AppSubsistemaEntity();
					subsistemaNou.setCodi(sin.getCodi());
					subsistemaNou.setNom(truncateString(sin.getNom(), 255));
					subsistemaNou.setActiu(true);
					subsistemaNou.setEntornApp(entornAppEntity);
					subsistemaRepository.save(subsistemaNou);
				}
			});
		}
		// Desactivam els subsistemes que no apareixen a la resposta
		subsistemesDb.forEach(sdb -> {
			Optional<SubsistemaInfo> subsistemaInfo = filteredSubsistemaInfos != null ? filteredSubsistemaInfos.stream().
					filter(sin -> sdb.getCodi().equals(sin.getCodi())).
					findFirst() : Optional.empty();
			if (subsistemaInfo.isEmpty()) {
				log.debug("\tDesactivant subsistema {}", sdb.getCodi());
				sdb.setActiu(false);
			}
		});
	}
}
