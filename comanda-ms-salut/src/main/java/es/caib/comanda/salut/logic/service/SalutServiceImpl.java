package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

	@Autowired
	private SalutIntegracioRepository salutIntegracioRepository;
	@Autowired
	private SalutSubsistemaRepository salutSubsistemaRepository;
	@Autowired
	private SalutMissatgeRepository salutMissatgeRepository;
	@Autowired
	private SalutDetallRepository salutDetallRepository;
	@Autowired
	private EntornAppServiceClient entornAppServiceClient;
	@Autowired
	private KeycloakHelper keycloakHelper;

	@PostConstruct
	public void init() {
		register(Salut.SALUT_REPORT_LAST, new InformeSalutLast());
		register(Salut.SALUT_REPORT_ESTAT, new InformeEstat());
		register(Salut.SALUT_REPORT_LATENCIA, new InformeLatencia());
	}

	protected void applyPerspectives(
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
	}

	/**
	 * Darrera informació de salut de cada aplicació/entorn.
	 */
	public class InformeSalutLast implements ReportGenerator<SalutEntity, Serializable, Salut> {
		@Override
		public List<Salut> generateData(String code, SalutEntity entity, Serializable params) throws ReportGenerationException {
			List<SalutEntity> saluts = ((SalutRepository)entityRepository).informeSalutLast(
				null,
				LocalDateTime.now());
			return entitiesToResources(saluts);
		}

		@Override
		public void onChange(Serializable previous, String fieldName, Object fieldValue, Map answers, String[] previousFieldNames, Serializable target) {
		}
	}

	private EntornApp entornAppFindById(Long entornAppId) {
		EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
				entornAppId,
				null,
				keycloakHelper.getAuthorizationHeader());
		if (entornApp != null) {
			return entornApp.getContent();
		}
		return null;
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
			List<SalutInformeEstatItem> data;
			if (SalutInformeAgrupacio.ANY == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeEstatAny(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MES == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeEstatMes(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.DIA == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeEstatDia(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.HORA == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeEstatHora(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MINUT == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeEstatMinut(
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

		@Override
		public void onChange(SalutInformeParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, SalutInformeParams target) {
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
			List<SalutInformeLatenciaItem> data;
			if (SalutInformeAgrupacio.ANY == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeLatenciaAny(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MES == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeLatenciaMes(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.DIA == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeLatenciaDia(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.HORA == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeLatenciaHora(
						params.getEntornAppId(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MINUT == params.getAgrupacio()) {
				data = ((SalutRepository)entityRepository).informeLatenciaMinut(
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

		@Override
		public void onChange(SalutInformeParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, SalutInformeParams target) {
		}
	}

}
