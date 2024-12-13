package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.configuracio.logic.intf.model.*;
import es.caib.comanda.configuracio.logic.intf.service.SalutService;
import es.caib.comanda.configuracio.persist.entity.SalutEntity;
import es.caib.comanda.configuracio.persist.repository.SalutRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementació del servei de consulta d'informació de salut.
 *
 * @author Limit Tecnologies
 */
@Service
public class SalutServiceImpl extends BaseReadonlyResourceService<Salut, Long, SalutEntity> implements SalutService {

	@PostConstruct
	public void init() {
		register(new InformeSalutLast());
		register(new InformeEstat());
		register(new InformeLatencia());
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
