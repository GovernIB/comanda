package es.caib.comanda.salut.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutEstatHelper;
import es.caib.comanda.salut.logic.intf.model.*;
import es.caib.comanda.salut.logic.intf.service.SalutService;
import es.caib.comanda.salut.persist.entity.*;
import es.caib.comanda.salut.persist.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Implementació del servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SalutServiceImpl extends BaseReadonlyResourceService<Salut, Long, SalutEntity> implements SalutService {

	private final SalutIntegracioRepository salutIntegracioRepository;
	private final SalutSubsistemaRepository salutSubsistemaRepository;
	private final SalutMissatgeRepository salutMissatgeRepository;
	private final SalutDetallRepository salutDetallRepository;
	private final SalutHistRepository salutHistRepository;
	private final SalutClientHelper salutClientHelper;
	private final MetricsHelper metricsHelper;
    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;
    private final SalutEstatHelper salutEstatHelper;

    @Override
    @Transactional
    public void netejaPerEntornApp(Long entornAppId) {
        List<Long> salutIds = ((SalutRepository) entityRepository).findIdsByEntornAppId(entornAppId);
        if (!salutIds.isEmpty()) {
            salutIntegracioRepository.deleteAllBySalutIdIn(salutIds);
            salutSubsistemaRepository.deleteAllBySalutIdIn(salutIds);
            salutMissatgeRepository.deleteAllBySalutIdIn(salutIds);
            salutDetallRepository.deleteAllBySalutIdIn(salutIds);
            ((SalutRepository) entityRepository).deleteAllByIdInBatch(salutIds);
        }
        salutHistRepository.deleteByEntornAppId(entornAppId);
    }

	@PostConstruct
	public void init() {
		register(Salut.SALUT_REPORT_LAST, new InformeSalutLast());
		register(Salut.SALUT_REPORT_ESTAT, new InformeEstat());
		register(Salut.SALUT_REPORT_ESTATS, new InformeEstats());
		register(Salut.SALUT_REPORT_LATENCIA, new InformeLatencia());
		register(Salut.SALUT_REPORT_GRUPS_DATES, new InformeGrupsDates());
		register(Salut.PERSP_INTEGRACIONS, new PerspectiveIntegracions());
		register(Salut.PERSP_SUBSISTEMES, new PerspectiveSubsistemes());
		register(Salut.PERSP_CONTEXTS, new PerspectiveContexts());
		register(Salut.PERSP_MISSATGES, new PerspectiveMissatges());
		register(Salut.PERSP_DETALLS, new PerspectiveDetalls());
		register(Salut.PERSP_HISTORICS, new PerspectiveHistorics());
        register(Salut.PERSP_ULTIM_ESTAT_OPERATIU_INFO, new PerspectiveUltimEstatOperatiuInfo());
	}

	@Override
	protected String additionalSpringFilter(
			String currentSpringFilter,
			String[] namedQueries) {
		if (authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
				|| authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)) {
			return null;
		}
		Set<Serializable> appPermissionIds = getAllowedIds(ResourceType.APP);
		Set<Serializable> entornAppPermissionIds = getAllowedIds(ResourceType.ENTORN_APP);
		Set<Long> allowedEntornAppIds = Collections.emptySet();
		LinkedHashSet<Long> mergedIds = new LinkedHashSet<>();
		if (!appPermissionIds.isEmpty()) {
			String appFilter = appPermissionIds.stream()
					.sorted(Comparator.comparingLong(id -> Long.parseLong(String.valueOf(id))))
					.map(String::valueOf)
					.map(id -> "app.id:" + id)
					.collect(Collectors.joining(" or "));
			salutClientHelper.entornAppFindByActivaTrue(appFilter)
					.forEach(entornApp -> mergedIds.add(entornApp.getId()));
		}
		if (!entornAppPermissionIds.isEmpty()) {
			String entornAppFilter = entornAppPermissionIds.stream()
					.sorted(Comparator.comparingLong(id -> Long.parseLong(String.valueOf(id))))
					.map(String::valueOf)
					.map(id -> "id:" + id)
					.collect(Collectors.joining(" or "));
			salutClientHelper.entornAppFindByActivaTrue(entornAppFilter)
					.forEach(entornApp -> mergedIds.add(entornApp.getId()));
		}
		allowedEntornAppIds = mergedIds;
		if (allowedEntornAppIds.isEmpty()) {
			return "entornAppId:0";
		}
		return allowedEntornAppIds.stream()
				.map(id -> "entornAppId:" + id)
				.collect(Collectors.joining(" or "));
	}

	private Set<Serializable> getAllowedIds(ResourceType resourceType) {
		return Optional.ofNullable(aclServiceClient.findIdsWithAnyPermission(
				resourceType,
				Collections.singletonList(PermissionEnum.READ),
				authenticationHelper.getCurrentUserName(),
				Arrays.asList(authenticationHelper.getCurrentUserRealmRoles()),
				httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
				.orElse(Collections.emptySet());
	}

	public class PerspectiveIntegracions implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			EntornApp entornAppForEntity = salutClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(entity.getEntornAppId());
			List<SalutIntegracioEntity> salutIntegracions = salutIntegracioRepository.findBySalutOrderByCodiAsc(entity);
			resource.setIntegracions(
				salutIntegracions.stream().
					map(i -> objectMappingHelper.newInstanceMap(
						i,
						SalutIntegracio.class,
						"salut")).
					collect(Collectors.toList()));
			if (entornAppForEntity != null && entornAppForEntity.getIntegracions() != null) {
				entornAppForEntity.getIntegracions().forEach(i -> {
					Optional<SalutIntegracio> salutIntegracio = resource.getIntegracions().stream().
						filter(si -> si.getCodi().equals(i.getCodi())).
						findFirst();
					salutIntegracio.ifPresent(integracio -> {
						integracio.setNom(i.getIntegracio().getNom());
						integracio.setLogo(i.getLogo());
					});
				});
			}
		}
	}

	public class PerspectiveSubsistemes implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			EntornApp entornAppForEntity = salutClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(entity.getEntornAppId());
			List<SalutSubsistemaEntity> salutSubsistemes = salutSubsistemaRepository.findBySalutOrderByCodiAsc(entity);
			resource.setSubsistemes(
				salutSubsistemes.stream().
					map(s -> objectMappingHelper.newInstanceMap(
						s,
						SalutSubsistema.class,
						"salut")).
					collect(Collectors.toList()));
			if (entornAppForEntity != null && entornAppForEntity.getSubsistemes() != null) {
				entornAppForEntity.getSubsistemes().forEach(s -> {
					Optional<SalutSubsistema> salutSubsistema = resource.getSubsistemes().stream().
						filter(ss -> ss.getCodi().equals(s.getCodi())).
						findFirst();
					salutSubsistema.ifPresent(subsistema -> subsistema.setNom(s.getNom()));
				});
			}
		}
	}

	public class PerspectiveContexts implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			EntornApp entornAppForEntity = salutClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(entity.getEntornAppId());
			resource.setContexts(entornAppForEntity.getContexts());
		}
	}

	public class PerspectiveMissatges implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			List<SalutMissatgeEntity> salutMissatges = salutMissatgeRepository.findBySalut(entity);
			if (salutMissatges == null)
				return;

			resource.setMissatges(
				salutMissatges.stream().
					map(s -> objectMappingHelper.newInstanceMap(
						s,
						SalutMissatge.class,
						"salut")).
					collect(Collectors.toList()));
		}
	}

	public class PerspectiveDetalls implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			List<SalutDetallEntity> salutDetalls = salutDetallRepository.findBySalut(entity);
			if (salutDetalls == null)
				return;

			resource.setDetalls(
				salutDetalls.stream().
					map(s -> objectMappingHelper.newInstanceMap(
						s,
						SalutDetall.class,
						"salut")).
					collect(Collectors.toList()));
		}

        @Override
        public boolean applyMultiple(String code, List<SalutEntity> entities, List<Salut> resources) throws PerspectiveApplicationException {
            for (SalutEntity salutEntity : entities) {
                Salut salut = resources.stream().filter(s-> salutEntity.getId().equals(s.getId())).findFirst().orElse(null);
                if (salut != null) {
                    this.applySingle(code, salutEntity, salut);
                }
            }
            return true;
        }
    }

    public class PerspectiveUltimEstatOperatiuInfo implements PerspectiveApplicator<SalutEntity, Salut> {
        private final List<SalutEstat> ESTATS_ESTABLES = List.of(
                SalutEstat.UP,
                SalutEstat.WARN,
                SalutEstat.DEGRADED,
		        SalutEstat.ERROR
        );

        @Override
        public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
            SalutHistEntity darrerHistoric = salutHistRepository.findTopByEntornAppIdOrderByDataDescIdDesc(entity.getEntornAppId());
            if (darrerHistoric == null || ESTATS_ESTABLES.contains(darrerHistoric.getAppEstat())) {
                return; //Si no te historic o esta en un estat estable no donarem informació
            }
            Optional<SalutHistEntity> salutHistEntity = salutHistRepository.findTopByEntornAppIdAndAppEstatInOrderByDataDesc(entity.getEntornAppId(), ESTATS_ESTABLES);
            if (salutHistEntity.isEmpty()) { return; } //Si no hi ha cap registre estable, no mostrarem informació
            resource.setUltimEstatInfo(new Salut.SalutEstatInfo(
                    salutHistEntity.get().getAppEstat(),
                    salutHistRepository.findSeguentData(entity.getEntornAppId(), salutHistEntity.get().getData()).orElse(darrerHistoric.getData())));
        }
    }

	public class PerspectiveHistorics implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			List<SalutHistEntity> salutHistorics = salutHistRepository.findByEntornAppIdOrderByDataDescIdDesc(entity.getEntornAppId());
			resource.setHistorics(
				salutHistorics.stream()
					.map(s -> objectMappingHelper.newInstanceMap(
						s,
						SalutHist.class))
					.collect(Collectors.toList()));
		}
	}

	/**
	 * Darrera informació de salut de cada aplicació/entorn.
	 */
	public class InformeSalutLast implements ReportGenerator<SalutEntity, String, Salut> {
		@Override
		public List<Salut> generateData(String code, SalutEntity entity, String params) throws ReportGenerationException {
			Instant t0 = Instant.now();
			List<EntornApp> entornApps = salutClientHelper.entornAppFindByActivaTrue(params);
			List<Long> entornAppIds = entornApps.stream()
					.filter(Objects::nonNull)
					.map(EntornApp::getId)
					.collect(Collectors.toList());
			metricsHelper.getSalutLastEntornAppsTimer().record(
					Duration.between(t0, Instant.now()));
			Instant t1 = Instant.now();
			List<SalutEntity> saluts = ((SalutRepository)entityRepository).informeSalutLast(
					entornAppIds,
					LocalDateTime.now());
			metricsHelper.getSalutLastDadesTimer().record(
					Duration.between(t1, Instant.now()));
			metricsHelper.getSalutLastGlobalTimer().record(
					Duration.between(t0, Instant.now()));
			if (saluts != null) {
				List<Salut> salutsResource = entitiesToResources(saluts);
                new PerspectiveDetalls().applyMultiple(null, saluts, salutsResource);
                return salutsResource;
			} else {
				return List.of();
			}
		}
		@Override
		public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
		}
	}

	/**
	 * Històric d'estats d'una aplicació entre dues dates.
	 * Paràmetres (tots obligatoris):
	 *   - appCodi: codi de l'aplicació.
	 *   - dataInici: data d'inici.
	 *   - dataFi: data de fi.
	 *   - agrupacio: agrupació temporal dels resultats.
	 */
	public class InformeEstat implements ReportGenerator<SalutEntity, SalutInformeParams, SalutInformeEstatItem> {
		@Override
		public List<SalutInformeEstatItem> generateData(
				String code,
				SalutEntity entity,
				SalutInformeParams params) throws ReportGenerationException {
            TipusRegistreSalut tipus = salutEstatHelper.mapTipusAgrupacio(params.getAgrupacio());
            LocalDateTime dataInici = salutEstatHelper.getDataIniciAjustada(params.getAgrupacio(), params.getDataReferencia());
			return salutEstatHelper.generateEstatList(dataInici, tipus, params.getEntornAppId());
		}

		@Override
		public void onChange(Serializable id, SalutInformeParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, SalutInformeParams target) {
		}
	}

    public class InformeEstats implements ReportGenerator<SalutEntity, SalutInformeLlistatParams, HashMap<String, Object>> {

        @Override
		public List<HashMap<String, Object>> generateData(String code, SalutEntity entity, SalutInformeLlistatParams params) throws ReportGenerationException {
            List<HashMap<String, Object>> result = new ArrayList<>();
            HashMap<String, Object> map = new HashMap<>();
            TipusRegistreSalut tipus = salutEstatHelper.mapTipusAgrupacio(params.getAgrupacio());
            LocalDateTime dataInici = salutEstatHelper.getDataIniciAjustada(params.getAgrupacio(), params.getDataReferencia());
            params.getEntornAppIdList().forEach(id -> {
                List<SalutInformeEstatItem> list = salutEstatHelper.generateEstatList(dataInici, tipus, id);
                map.put(String.valueOf(id), list);
            });
            result.add(map);
			return result;
		}

        @Override
        public void onChange(Serializable id, SalutInformeLlistatParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, SalutInformeLlistatParams target) {
        }
    }

	/**
	 * Mitja de la latencia agrupada d'una aplicació entre dues dates.
	 * Paràmetres (tots obligatoris):
	 *   - appCodi: codi de l'aplicació.
	 *   - dataInici: data d'inici.
	 *   - dataFi: data de fi.
	 *   - agrupacio: agrupació temporal dels resultats.
	 */
	public class InformeLatencia implements ReportGenerator<SalutEntity, SalutInformeParams, SalutInformeLatenciaItem> {
		@Override
		public List<SalutInformeLatenciaItem> generateData(
				String code,
				SalutEntity entity,
				SalutInformeParams params) throws ReportGenerationException {
			final List<SalutInformeLatenciaItem> data = new ArrayList<>();

            TipusRegistreSalut tipus = salutEstatHelper.mapTipusAgrupacio(params.getAgrupacio());
            LocalDateTime dataInici = salutEstatHelper.getDataIniciAjustada(params.getAgrupacio(), params.getDataReferencia());

            List<SalutEntity> salutEntityList = ((SalutRepository) entityRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                    params.getEntornAppId(),
                    dataInici,
                    tipus);

            // Comportament per defecte per a altres agrupacions
            salutEntityList.forEach(salutEntity -> {
                data.add(new SalutInformeLatenciaItem(salutEntity));
            });

            return data;
		}

		@Override
		public void onChange(Serializable id, SalutInformeParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, SalutInformeParams target) {
		}
	}

    public class InformeGrupsDates implements ReportGenerator<SalutEntity, SalutInformeGrupsParams, SalutInformeGrupItem> {

        @Override
        public List<SalutInformeGrupItem> generateData(String code, SalutEntity entity, SalutInformeGrupsParams params) throws ReportGenerationException {
            LocalDateTime dataInici = salutEstatHelper.getDataIniciAjustada(params.getAgrupacio(), params.getDataReferencia());
            return salutEstatHelper.generarGrupsDates(dataInici, params.getAgrupacio()).stream()
                    .map(SalutInformeGrupItem::new)
                    .collect(Collectors.toList());
        }

        @Override
        public void onChange(Serializable id, SalutInformeGrupsParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, SalutInformeGrupsParams target) {
        }
    }
}
