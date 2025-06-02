package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
	private ConfiguracioSchedulerService schedulerService;

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

	@Override
	protected void afterUpdateSave(AppEntity entity, App resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
		super.afterUpdateSave(entity, resource, answers, anyOrderChanged);

		// Per assegurar que s'executin les tasques programades correctament, es programen de nou.
		if (entity.getEntornApps() != null && !entity.getEntornApps().isEmpty()) {
			entity.getEntornApps().forEach(e -> {
				schedulerService.programarTasca(e);
				appInfoHelper.programarTasquesSalutEstadistica(e);
			});
		}
	}

}
