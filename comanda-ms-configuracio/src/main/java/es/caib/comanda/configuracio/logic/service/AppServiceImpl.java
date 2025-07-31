package es.caib.comanda.configuracio.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.model.export.AppExport;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.logic.mapper.AppExportMapper;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FieldOption;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementació del servei de gestió d'aplicacions.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AppServiceImpl extends BaseMutableResourceService<App, Long, AppEntity> implements AppService {

	private final AppInfoHelper appInfoHelper;
	private final ConfiguracioSchedulerService schedulerService;
	private final ObjectMapper objectMapper;
	private final AppExportMapper appExportMapper;

	@PostConstruct
	public void init() {
		register(App.APP_EXPORT, new AppExportReportGenerator());
		register(App.APP_IMPORT, new AppImportActionExecutor());
	}
	
	/**
	 * Generador d'informes per exportar aplicacions en format JSON.
	 */
	public class AppExportReportGenerator implements ReportGenerator<AppEntity, Serializable, AppExport> {
		@Override
		public List<AppExport> generateData(String code, AppEntity entity, Serializable params) throws ReportGenerationException {
			List<AppExport> result = new ArrayList<>();
			
			// Si s'ha especificat una entitat, només exportem aquesta
			if (entity != null) {
				AppExport app = appExportMapper.toExport(entity);
				result.add(app);
			} else {
				// Si no s'ha especificat una entitat, exportem totes les aplicacions
				List<AppEntity> entities = entityRepository.findAll();
				List<AppExport> apps = appExportMapper.toExport(entities);
				result.addAll(apps);
			}
			
			return result;
		}
		
		@Override
		public DownloadableFile generateFile(String code, List<?> data, ReportFileType fileType, OutputStream out) {
			try {
				// Utilitzem un ByteArrayOutputStream per capturar el contingut
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				objectMapper.writerWithDefaultPrettyPrinter().writeValue(baos, data);
				
				// Escrivim el contingut a l'OutputStream original
				byte[] content = baos.toByteArray();
				out.write(content);
				
				return new DownloadableFile("aplicacions.json", "application/json", content);
			} catch (IOException e) {
				log.error("Error generating JSON file", e);
				return null;
			}
		}
		
		@Override
		public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
			// No es necessari implementar aquest mètode
		}
	}
	
	/**
	 * Paràmetres per a la importació d'aplicacions.
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AppImportParams implements Serializable {
		private String jsonContent;
		private boolean overwrite;
	}
	
	/**
	 * Classe que encapsula una llista d'aplicacions per a la importació/exportació.
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AppImportResult implements Serializable {
		private List<App> apps;
	}
	
	/**
	 * ActionExecutor per a la importació d'aplicacions des d'un fitxer JSON.
	 * Aquesta classe permet importar aplicacions i els seus entorns relacionats.
	 */
	public class AppImportActionExecutor implements ActionExecutor<AppEntity, AppImportParams, AppImportResult> {
		@Override
		public AppImportResult exec(String code, AppEntity entity, AppImportParams params) {
			try {
				List<App> appsToImport = objectMapper.readValue(params.getJsonContent(), 
						objectMapper.getTypeFactory().constructCollectionType(List.class, App.class));
				
				List<App> importedApps = new ArrayList<>();
				Map<String, AnswerRequiredException.AnswerValue> emptyAnswers = new HashMap<>();
				
				for (App appToImport : appsToImport) {
					// Comprovar si l'aplicació ja existeix
					AppEntity existingApp = ((AppRepository)entityRepository).findByCodi(appToImport.getCodi());
					
					if (existingApp != null) {
						if (params.isOverwrite()) {
							// Actualitzar l'aplicació existent
							App existingAppResource = entityToResource(existingApp);
							existingAppResource.setNom(appToImport.getNom());
							existingAppResource.setDescripcio(appToImport.getDescripcio());
							existingAppResource.setActiva(appToImport.isActiva());
							existingAppResource.setLogo(appToImport.getLogo());
							
							// Guardar l'aplicació actualitzada
							update(existingAppResource.getId(), existingAppResource, emptyAnswers);
							importedApps.add(existingAppResource);
						}
					} else {
						// Crear una nova aplicació
						App createdApp = create(appToImport, emptyAnswers);
						importedApps.add(createdApp);
					}
				}
				
				return new AppImportResult(importedApps);
			} catch (Exception e) {
				log.error("Error importing apps from JSON", e);
				throw new RuntimeException("Error importing apps: " + e.getMessage(), e);
			}
		}
		
		@Override
		public void onChange(Serializable id, AppImportParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, AppImportParams target) {
			// No es necessari implementar aquest mètode
		}
		
		@Override
		public List<FieldOption> getOptions(String fieldName, Map<String, String[]> requestParameterMap) {
			// No es necessari implementar aquest mètode
			return new ArrayList<>();
		}
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
