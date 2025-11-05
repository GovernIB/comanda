package es.caib.comanda.configuracio.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.App.AppImportForm;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.model.export.AppExport;
import es.caib.comanda.configuracio.logic.intf.model.export.EntornAppExport;
import es.caib.comanda.configuracio.logic.intf.service.AppService;
import es.caib.comanda.configuracio.logic.mapper.AppExportMapper;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.helper.CacheHelper;
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

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.APP_CACHE;

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
	private final CacheHelper cacheHelper;
	private final ObjectMapper objectMapper;
	private final AppExportMapper appExportMapper;
	private final AppRepository appRepository;
	private final EntornRepository entornRepository;
	private final EntornAppRepository entornAppRepository;

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

                // Genera nom del fitxer
                String exportFileName = "aplicacions.json";
                try {
                    if (data != null && data.size() == 1) {
                        exportFileName = ((AppExport)data.get(0)).getNom() + ".json";
                    }
                } catch (Exception ex) {
                    log.error("Error generant el nom del fitxer d'exportació de Apps", ex);
                }
				
				return new DownloadableFile(exportFileName, "application/json", content);
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
	public class AppImportActionExecutor implements ActionExecutor<AppEntity, AppImportForm, AppImportResult> {
		@Override
		public AppImportResult exec(String code, AppEntity entity, AppImportForm params) {
			try {
				// Parse JSON as list of AppExport; if single object, wrap it
				List<AppExport> exports;
				try {
					exports = objectMapper.readValue(
							params.getJsonContent(),
							objectMapper.getTypeFactory().constructCollectionType(List.class, AppExport.class));
				} catch (Exception exList) {
					AppExport single = objectMapper.readValue(params.getJsonContent(), AppExport.class);
					exports = new ArrayList<>();
					exports.add(single);
				}
				List<App> resultResources = new ArrayList<>();
				for (AppExport appExport : exports) {
					if (appExport.getCodi() == null || appExport.getCodi().isEmpty()) {
						throw new IllegalArgumentException("El camp 'codi' de l'aplicació és obligatori en la importació");
					}
					String decision = params.getDecision();
					AppEntity appEntity = appRepository.findByCodi(appExport.getCodi());
					boolean exists = appEntity != null;
					if (exists && (decision == null || decision.isEmpty())) {
						List<AnswerRequiredException.CustomAnswer> choices = List.of(
								new AnswerRequiredException.CustomAnswer("OVERWRITE", "Sobreescriure"),
								new AnswerRequiredException.CustomAnswer("COMBINE", "Combinar entorns (afegeix els inexistents)"),
								new AnswerRequiredException.CustomAnswer("SKIP", "Ometre")
						);
						throw new AnswerRequiredException(
								App.class,
								"app-import-decision",
								"L'aplicació amb codi '" + appExport.getCodi() + "' ja existeix. Què vols fer?",
								choices);
					}
					if (!exists) {
						// Crear nova AppEntity
						appEntity = new AppEntity();
						appEntity.setCodi(appExport.getCodi());
					}
					// Actualitzar camps bàsics (també en OVERWRITE i COMBINE si cal actualitzar nom/flags)
					if (appExport.getNom() != null) appEntity.setNom(appExport.getNom());
					if (appExport.getDescripcio() != null) appEntity.setDescripcio(appExport.getDescripcio());
					appEntity.setActiva(appExport.isActiva());
					if (appExport.getLogo() != null) appEntity.setLogo(appExport.getLogo());
					// Persist base app before handling entornApps to ensure id exists
					appEntity = appRepository.saveAndFlush(appEntity);
					appRepository.refresh(appEntity);
					// Gestionar entornApps segons decisió
					if (appExport.getEntornApps() != null) {
						if (exists) {
							if ("OVERWRITE".equalsIgnoreCase(decision)) {
								applyOverwrite(appEntity, appExport);
							} else if ("COMBINE".equalsIgnoreCase(decision)) {
								applyCombine(appEntity, appExport);
							} else if ("SKIP".equalsIgnoreCase(decision)) {
								// No canviar entornApps
							} else if (decision == null || decision.isEmpty()) {
								// For new apps, no decision required, treat as overwrite semantics
								applyOverwrite(appEntity, appExport);
							} else {
								// Valor desconegut: error de validació
								throw new IllegalArgumentException("Valor de 'decision' desconegut: " + decision);
							}
						} else {
							// Nova app → crear totes les relacions
							applyOverwrite(appEntity, appExport);
						}
					}
					// Reprogramar tasques per entornApps d'aquesta app
					if (appEntity.getEntornApps() != null) {
						for (EntornAppEntity eae : appEntity.getEntornApps()) {
							schedulerService.programarTasca(eae);
							appInfoHelper.programarTasquesSalutEstadistica(eae);
						}
					}
					// Invalidar caché
					cacheHelper.evictCacheItem(APP_CACHE, appEntity.getId().toString());
					// Afegir a resultat com a recurs
					resultResources.add(entityToResource(appEntity));
				}
				AppImportResult result = new AppImportResult(resultResources);
				return result;
			} catch (AnswerRequiredException are) {
				throw are;
			} catch (Exception e) {
				log.error("Error importing apps from JSON", e);
				throw new RuntimeException("Error importing apps: " + e.getMessage(), e);
			}
		}

		private void applyOverwrite(AppEntity appEntity, AppExport appExport) {
			// Map dels entorns importats per codi
			Map<String, EntornAppExport> importByEntorn = new HashMap<>();
			for (EntornAppExport e : appExport.getEntornApps()) {
				importByEntorn.put(e.getEntornCodi(), e);
			}
			// Eliminar associacions que ja no estan a l'import
			List<EntornAppEntity> existing = appEntity.getEntornApps() != null ? new ArrayList<>(appEntity.getEntornApps()) : new ArrayList<>();
			for (EntornAppEntity eae : existing) {
				String codi = eae.getEntorn().getCodi();
				if (!importByEntorn.containsKey(codi)) {
					entornAppRepository.delete(eae);
				}
			}
			// Crear o actualitzar les associacions importades
			for (EntornAppExport e : appExport.getEntornApps()) {
				EntornEntity ent = resolveOrCreateEntorn(e.getEntornCodi(), e.getEntornNom());
				EntornAppEntity eae = entornAppRepository.findByEntornIdAndAppId(ent.getId(), appEntity.getId()).orElse(null);
				if (eae == null) {
					eae = new EntornAppEntity();
					eae.setApp(appEntity);
					eae.setEntorn(ent);
				}
				fillEntornAppFromExport(e, eae);
				entornAppRepository.save(eae);
			}
		}

		private void applyCombine(AppEntity appEntity, AppExport appExport) {
			// Només afegir les associacions que no existeixen
			for (EntornAppExport e : appExport.getEntornApps()) {
				EntornEntity ent = resolveOrCreateEntorn(e.getEntornCodi(), e.getEntornNom());
				if (entornAppRepository.findByEntornIdAndAppId(ent.getId(), appEntity.getId()).isEmpty()) {
					EntornAppEntity eae = new EntornAppEntity();
					eae.setApp(appEntity);
					eae.setEntorn(ent);
					fillEntornAppFromExport(e, eae);
					entornAppRepository.save(eae);
				}
			}
		}

		private EntornEntity resolveOrCreateEntorn(String codi, String nom) {
			EntornEntity ent = entornRepository.findByCodi(codi);
			if (ent == null) {
				ent = new EntornEntity();
				ent.setCodi(codi);
				ent.setNom(nom != null ? nom : codi);
				ent = entornRepository.saveAndFlush(ent);
			}
			return ent;
		}

		private void fillEntornAppFromExport(EntornAppExport e, EntornAppEntity target) {
			target.setInfoUrl(e.getInfoUrl());
			target.setActiva(e.isActiva());
			target.setSalutUrl(e.getSalutUrl());
			target.setEstadisticaInfoUrl(e.getEstadisticaInfoUrl());
			target.setEstadisticaUrl(e.getEstadisticaUrl());
			target.setEstadisticaCron(e.getEstadisticaCron());
			// Compactació
			if (e.getCompactable() != null) target.setCompactable(e.getCompactable());
			target.setCompactacioSetmanalMesos(e.getCompactacioSetmanalMesos());
			target.setCompactacioMensualMesos(e.getCompactacioMensualMesos());
			target.setEliminacioMesos(e.getEliminacioMesos());
		}

		@Override
		public void onChange(Serializable id, AppImportForm previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, AppImportForm target) {
			// No es necessari implementar aquest mètode
		}
		
		@Override
		public List<FieldOption> getOptions(String fieldName, Map<String, String[]> requestParameterMap) {
			if ("decision".equals(fieldName)) {
				List<FieldOption> options = new ArrayList<>();
				options.add(new FieldOption("OVERWRITE", "Sobreescriure"));
				options.add(new FieldOption("COMBINE", "Combinar entorns (afegeix els inexistents)"));
				options.add(new FieldOption("SKIP", "Ometre"));
				return options;
			}
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
		cacheHelper.evictCacheItem(APP_CACHE, entity.getId().toString());

		// Per assegurar que s'executin les tasques programades correctament, es programen de nou.
		if (entity.getEntornApps() != null && !entity.getEntornApps().isEmpty()) {
			entity.getEntornApps().forEach(entornAppEntity -> {
                if (!entity.isActiva()) {
//                    Setejam entornApp actiu a false per a no es tornin a reprogramar les tasques sobre l'entornApp esborrat
                    entornAppEntity.setActiva(false);
                }

				schedulerService.programarTasca(entornAppEntity);
				appInfoHelper.programarTasquesSalutEstadistica(entornAppEntity);
			});
		}
	}

    @Override
    protected void afterDelete(AppEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        super.afterDelete(entity, answers);
        cacheHelper.evictCacheItem(APP_CACHE, entity.getId().toString());

        if (entity.getEntornApps() != null && !entity.getEntornApps().isEmpty()) {
            entity.getEntornApps().forEach(entornAppEntity -> {
//                Setejam entornApp actiu a false per a no es tornin a reprogramar les tasques sobre l'entornApp esborrat
                entornAppEntity.setActiva(false);

                schedulerService.programarTasca(entornAppEntity);
                appInfoHelper.programarTasquesSalutEstadistica(entornAppEntity);
            });
        }
    }
}
