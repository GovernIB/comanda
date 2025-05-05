package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutDetall;
import es.caib.comanda.salut.logic.intf.model.SalutInformeAgrupacio;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem;
import es.caib.comanda.salut.logic.intf.model.SalutInformeParams;
import es.caib.comanda.salut.logic.intf.model.SalutIntegracio;
import es.caib.comanda.salut.logic.intf.model.SalutMissatge;
import es.caib.comanda.salut.logic.intf.model.SalutSubsistema;
import es.caib.comanda.salut.logic.intf.service.SalutService;
import es.caib.comanda.salut.persist.entity.SalutDetallEntity;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.salut.persist.entity.SalutSubsistemaEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementació del servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class SalutServiceImpl extends BaseReadonlyResourceService<Salut, Long, SalutEntity> implements SalutService {

	private static final String PERSP_INTEGRACIONS = "SAL_INTEGRACIONS";
	private static final String PERSP_SUBSISTEMES = "SAL_SUBSISTEMES";
	private static final String PERSP_MISSATGES = "SAL_MISSATGES";
	private static final String PERSP_DETALLS = "SAL_DETALLS";

	@Value("${es.caib.comanda.keycloak.username:#{null}}")
	private String keycloakUsername;
	@Value("${es.caib.comanda.keycloak.password:#{null}}")
	private String keycloakPassword;

	@Autowired
	private SalutIntegracioRepository salutIntegracioRepository;
	@Autowired
	private SalutSubsistemaRepository salutSubsistemaRepository;
	@Autowired
	private SalutMissatgeRepository salutMissatgeRepository;
	@Autowired
	private SalutDetallRepository salutDetallRepository;
	@Autowired
	private SalutInfoHelper salutInfoHelper;
	@Autowired
	private AppServiceClient appServiceClient;
	@Autowired
	private EntornAppServiceClient entornAppServiceClient;
	@Autowired
	private KeycloakHelper keycloakHelper;

	@PostConstruct
	public void init() {
		register(new InformeSalutLast());
		register(new InformeEstat());
		register(new InformeLatencia());
	}

	@Override
//	@Transactional
	public void getSalutInfo() {
		log.debug("Iniciant consulta periòdica de salut");
		List<App> apps = appFindByActivaTrue();
		apps.forEach(a -> {
			if (a.getEntornApps().isEmpty())
				return;

			a.getEntornApps().parallelStream().forEach(ea -> {
				if (ea.isActiva()) {
					try {
						salutInfoHelper.getSalutInfo(ea, ea.getSalutUrl());
					} catch (Exception ex) {
						log.error("No s'ha pogut consultar la salut de l'aplicació {}", a.getCodi(), ex);
					}
				}
			});
		});
	}

	protected Salut applyPerspectives(
			SalutEntity entity,
			Salut resource,
			String[] perspectives) {
		boolean integracionsActive = Arrays.asList(perspectives).contains(PERSP_INTEGRACIONS);
		boolean subsistemesActive = Arrays.asList(perspectives).contains(PERSP_SUBSISTEMES);
		EntornApp entornAppForEntity = null;
		if (integracionsActive) {
			entornAppForEntity = entornAppFindById(entity.getEntornAppId());
			List<SalutIntegracioEntity> salutIntegracions = salutIntegracioRepository.findBySalut(entity);
			resource.setIntegracions(
					salutIntegracions.stream().
							map(i -> objectMappingHelper.newInstanceMap(
									i,
									SalutIntegracio.class,
									"salut")).
							collect(Collectors.toList()));
			if (entornAppForEntity != null) {
				entornAppForEntity.getIntegracions().forEach(i -> {
					Optional<SalutIntegracio> salutIntegracio = resource.getIntegracions().stream().
							filter(si -> si.getCodi().equals(i.getCodi())).
							findFirst();
					salutIntegracio.ifPresent(integracio -> integracio.setNom(i.getNom()));
				});
			}
		}
		if (subsistemesActive) {
			if (entornAppForEntity == null) {
				entornAppForEntity = entornAppFindById(entity.getEntornAppId());
			}
			List<SalutSubsistemaEntity> salutSubsistemes = salutSubsistemaRepository.findBySalut(entity);
			resource.setSubsistemes(
					salutSubsistemes.stream().
							map(s -> objectMappingHelper.newInstanceMap(
									s,
									SalutSubsistema.class,
									"salut")).
							collect(Collectors.toList()));
			if (entornAppForEntity != null) {
				entornAppForEntity.getSubsistemes().forEach(s -> {
					Optional<SalutSubsistema> salutSubsistema = resource.getSubsistemes().stream().
							filter(ss -> ss.getCodi().equals(s.getCodi())).
							findFirst();
					salutSubsistema.ifPresent(subsistema -> subsistema.setNom(s.getNom()));
				});
			}
		}
		boolean missatgesActive = Arrays.asList(perspectives).contains(PERSP_MISSATGES);
		if (missatgesActive) {
			List<SalutMissatgeEntity> salutMissatges = salutMissatgeRepository.findBySalut(entity);
			resource.setMissatges(
					salutMissatges.stream().
							map(s -> objectMappingHelper.newInstanceMap(
									s,
									SalutMissatge.class,
									"salut")).
							collect(Collectors.toList()));
		}
		boolean detallsActive = Arrays.asList(perspectives).contains(PERSP_DETALLS);
		if (detallsActive) {
			List<SalutDetallEntity> salutDetalls = salutDetallRepository.findBySalut(entity);
			resource.setDetalls(
					salutDetalls.stream().
							map(s -> objectMappingHelper.newInstanceMap(
									s,
									SalutDetall.class,
									"salut")).
							collect(Collectors.toList()));
		}
		return null;
	}

	/**
	 * Darrera informació de salut de cada aplicació/entorn.
	 */
	public class InformeSalutLast implements ReportDataGenerator<Object, Salut> {
		@Override
		public String[] getSupportedReportCodes() {
			return new String[] { "salut_last" };
		}
		@Override
		public Class<Object> getParameterClass() {
			return null;
		}
		@Override
		public List<Salut> generate(
				String code,
				Object params) throws ReportGenerationException {
			List<SalutEntity> saluts = ((SalutRepository)resourceRepository).informeSalutLast(
					null,
					LocalDateTime.now());
			return entitiesToResources(saluts);
		}
	}

	private List<App> appFindByActivaTrue() {
		PagedModel<EntityModel<App>> apps = appServiceClient.find(
				null,
				"activa:true",
				null,
				null,
				"UNPAGED",
				null,
				getAuthorizationHeader());
		return apps.getContent().stream().
				map(EntityModel::getContent).
				collect(Collectors.toList());
	}

//	private App appFindByCodi(String codi) {
//		PagedModel<EntityModel<App>> apps = appServiceClient.find(
//				null,
//				"codi:'" + codi + "'",
//				null,
//				null,
//				"UNPAGED",
//				null,
//				getAuthorizationHeader());
//		if (!apps.getContent().isEmpty()) {
//			return apps.getContent().iterator().next().getContent();
//		} else {
//			return null;
//		}
//	}

	private EntornApp entornAppFindById(Long entornAppId) {
		EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
				entornAppId,
				null,
				getAuthorizationHeader());
		if (entornApp != null) {
			return entornApp.getContent();
		}
		return null;
	}

	private String getAuthorizationHeader() {
		String accessToken = keycloakHelper.getAccessTokenWithUsernamePassword(
				keycloakUsername,
				keycloakPassword);
		return accessToken != null ? "Bearer " + accessToken : null;
	}

	/**
	 * Històric d'estats d'una aplicació entre dues dates.
	 * Paràmetres (tots obligatoris):
	 *   - appCodi: codi de l'aplicació.
	 *   - dataInici: data d'inici.
	 *   - dataFi: data de fi.
	 *   - agrupacio: agrupació temporal dels resultats.
	 */
	public class InformeEstat implements ReportDataGenerator<SalutInformeParams, SalutInformeEstatItem> {
		@Override
		public String[] getSupportedReportCodes() {
			return new String[] { "estat" };
		}
		@Override
		public Class<SalutInformeParams> getParameterClass() {
			return SalutInformeParams.class;
		}
		@Override
		public List<SalutInformeEstatItem> generate(
				String code,
				SalutInformeParams params) throws ReportGenerationException {
			List<SalutInformeEstatItem> data;
			if (SalutInformeAgrupacio.ANY == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatAny(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MES == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatMes(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.DIA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatDia(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.HORA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatHora(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MINUT == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatMinut(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else {
				throw new ReportGenerationException(
						Salut.class,
						null,
						code,
						"Unknown agrupacio value: " + params.getAgrupacio());
			}
			return data;
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
	public class InformeLatencia implements ReportDataGenerator<SalutInformeParams, SalutInformeLatenciaItem> {
		@Override
		public String[] getSupportedReportCodes() {
			return new String[] { "latencia" };
		}
		@Override
		public Class<SalutInformeParams> getParameterClass() {
			return SalutInformeParams.class;
		}
		@Override
		public List<SalutInformeLatenciaItem> generate(
				String code,
				SalutInformeParams params) throws ReportGenerationException {
			List<SalutInformeLatenciaItem> data;
			if (SalutInformeAgrupacio.ANY == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaAny(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MES == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaMes(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.DIA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaDia(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.HORA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaHora(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MINUT == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaMinut(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else {
				throw new ReportGenerationException(
						Salut.class,
						null,
						code,
						"Unknown agrupacio value: " + params.getAgrupacio());
			}
			return data;
		}
	}

}
