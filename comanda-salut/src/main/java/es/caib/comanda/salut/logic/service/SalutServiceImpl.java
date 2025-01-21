package es.caib.comanda.salut.logic.service;

import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.*;
import es.caib.comanda.salut.logic.intf.service.SalutService;
import es.caib.comanda.salut.persist.entity.*;
import es.caib.comanda.salut.persist.repository.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementació del servei de consulta d'informació de salut.
 *
 * @author Limit Tecnologies
 */
@Slf4j
@Service
public class SalutServiceImpl extends BaseReadonlyResourceService<Salut, Long, SalutEntity> implements SalutService {

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

	@PostConstruct
	public void init() {
		register(new InformeSalutLast());
		register(new InformeEstat());
		register(new InformeLatencia());
	}

	@Override
	@Transactional
	public void getSalutInfo() {
		log.debug("Iniciant consulta periòdica de salut");
		List<App> apps = appFindByActivaTrue();
		apps.forEach(a -> {
			try {
				salutInfoHelper.getSalutInfo(a.getCodi(), a.getSalutUrl());
			} catch (Exception ex) {
				log.error("No s'ha pogut consultar la salut de l'aplicació {}", a.getCodi(), ex);
			}
		});
	}

	protected Salut applyPerspectives(
			SalutEntity entity,
			Salut resource,
			String[] perspectives) {
		boolean integracionsActive = Arrays.asList(perspectives).contains(Salut.PERSP_INTEGRACIONS);
		if (integracionsActive) {
			List<SalutIntegracioEntity> salutIntegracions = salutIntegracioRepository.findBySalut(entity);
			resource.setIntegracions(
					salutIntegracions.stream().
							map(i -> objectMappingHelper.newInstanceMap(
									i,
									SalutIntegracio.class,
									"salut")).
							collect(Collectors.toList()));
			/*integracioRepository.findByAppCodi(entity.getCodi()).forEach(i -> {
				Optional<SalutIntegracio> salutIntegracio = resource.getIntegracions().stream().
						filter(si -> si.getCodi().equals(i.getCodi())).
						findFirst();
				salutIntegracio.ifPresent(integracio -> integracio.setNom(i.getNom()));
			});*/
		}
		boolean subsistemesActive = Arrays.asList(perspectives).contains(Salut.PERSP_SUBSISTEMES);
		if (subsistemesActive) {
			List<SalutSubsistemaEntity> salutSubsistemes = salutSubsistemaRepository.findBySalut(entity);
			resource.setSubsistemes(
					salutSubsistemes.stream().
							map(s -> objectMappingHelper.newInstanceMap(
									s,
									SalutSubsistema.class,
									"salut")).
							collect(Collectors.toList()));
			/*subsistemaRepository.findByAppCodi(entity.getCodi()).forEach(s -> {
				Optional<SalutSubsistema> salutSubsistema = resource.getSubsistemes().stream().
						filter(ss -> ss.getCodi().equals(s.getCodi())).
						findFirst();
				salutSubsistema.ifPresent(subsistema -> subsistema.setNom(s.getNom()));
			});*/
		}
		boolean missatgesActive = Arrays.asList(perspectives).contains(Salut.PERSP_MISSATGES);
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
		boolean detallsActive = Arrays.asList(perspectives).contains(Salut.PERSP_DETALLS);
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
	 * Darrera informació de salut de cada aplicació.
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
		List<App> activeApps = new ArrayList<>();
		activeApps.add(notibApp());
		return activeApps;
	}

	/*private App appFindByCode(String code) {
		if ("NOT".equals(code)) {
			return notibApp();
		} else {
			return null;
		}
	}*/

	private App notibApp() {
		return new App(
				"NOT",
				"Notib",
				null,
				"https://dev.caib.es/notibapi/interna/appInfo",
				1,
				null,
				"https://dev.caib.es/notibapi/interna/salut",
				1,
				"1.0",
				true);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	private static class App {
		private String codi;
		private String nom;
		private String descripcio;
		private String infoUrl;
		private Integer infoInterval = 1;
		private LocalDateTime infoData;
		private String salutUrl;
		private Integer salutInterval = 1;
		private String versio;
		private boolean activa;
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
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MES == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatMes(
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.DIA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatDia(
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.HORA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatHora(
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MINUT == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeEstatMinut(
						params.getAppCodi(),
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
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MES == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaMes(
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.DIA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaDia(
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.HORA == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaHora(
						params.getAppCodi(),
						params.getDataInici(),
						params.getDataFi());
			} else if (SalutInformeAgrupacio.MINUT == params.getAgrupacio()) {
				data = ((SalutRepository)resourceRepository).informeLatenciaMinut(
						params.getAppCodi(),
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
