package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.intf.model.*;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static es.caib.comanda.salut.logic.helper.SalutInfoHelper.MINUTS_PER_AGRUPACIO;

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
	private final SalutClientHelper salutClientHelper;
	private final MetricsHelper metricsHelper;

	@PostConstruct
	public void init() {
		register(Salut.SALUT_REPORT_LAST, new InformeSalutLast());
		register(Salut.SALUT_REPORT_ESTAT, new InformeEstat());
		register(Salut.SALUT_REPORT_ESTATS, new InformeEstats());
		register(Salut.SALUT_REPORT_LATENCIA, new InformeLatencia());
		register(Salut.PERSP_INTEGRACIONS, new PerspectiveIntegracions());
		register(Salut.PERSP_SUBSISTEMES, new PerspectiveSubsistemes());
		register(Salut.PERSP_CONTEXTS, new PerspectiveContexts());
		register(Salut.PERSP_MISSATGES, new PerspectiveMissatges());
		register(Salut.PERSP_DETALLS, new PerspectiveDetalls());
	}

	public class PerspectiveIntegracions implements PerspectiveApplicator<SalutEntity, Salut> {
		@Override
		public void applySingle(String code, SalutEntity entity, Salut resource) throws PerspectiveApplicationException {
			EntornApp entornAppForEntity = salutClientHelper.entornAppFindById(entity.getEntornAppId());
			List<SalutIntegracioEntity> salutIntegracions = salutIntegracioRepository.findBySalut(entity);
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
			EntornApp entornAppForEntity = salutClientHelper.entornAppFindById(entity.getEntornAppId());
			List<SalutSubsistemaEntity> salutSubsistemes = salutSubsistemaRepository.findBySalut(entity);
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
			EntornApp entornAppForEntity = salutClientHelper.entornAppFindById(entity.getEntornAppId());
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
				return entitiesToResources(saluts);
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
            TipusRegistreSalut tipus = mapTipusAgrupacio(params.getAgrupacio());
            LocalDateTime dataInici = calculaDataIniciAmbMarge(params.getDataInici(), tipus);
            if (tipus == TipusRegistreSalut.MINUTS && isFranjaMinutsInvalida(dataInici, params.getDataFi())) {
				throw new ReportGenerationException(Salut.class, "Rang de dades invàlid. DataInici i DataFi han de tenir un múltiple de 4 com a minuts");
            }

			return generateSalutEstatList(dataInici, tipus, params.getEntornAppId());
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
            TipusRegistreSalut tipus = mapTipusAgrupacio(params.getAgrupacio());

            params.getEntornAppIdList().forEach( id -> {
                List<SalutInformeEstatItem> list = generateSalutEstatList(params.getDataInici(), tipus, id);
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

            TipusRegistreSalut tipus = mapTipusAgrupacio(params.getAgrupacio());
            LocalDateTime dataInici = calculaDataIniciAmbMarge(params.getDataInici(), tipus);

            List<SalutEntity> salutEntityList = ((SalutRepository) entityRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                    params.getEntornAppId(),
                    dataInici,
                    tipus);

            if (tipus == TipusRegistreSalut.MINUTS && isFranjaMinutsInvalida(dataInici, params.getDataFi())) {
                throw new ReportGenerationException(Salut.class, "Rang de dades invàlid. DataInici i DataFi han de tenir un múltiple de 4 com a minuts");
            }

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

    // --------------------------------------------------------------------------------------------
    // Mètodes auxiliars per evitar duplicació entre informes
    // --------------------------------------------------------------------------------------------

    /**
     * Converteix l'agrupació de l'informe al TipusRegistreSalut corresponent.
     */
    private TipusRegistreSalut mapTipusAgrupacio(SalutInformeAgrupacio agrupacio) throws ReportGenerationException {
        switch (agrupacio) {
            case MINUT: return TipusRegistreSalut.MINUT;
            case MINUTS_HORA: return TipusRegistreSalut.MINUTS;
            case HORA: return TipusRegistreSalut.HORA;
            case DIA_SETMANA:
            case DIA_MES: return TipusRegistreSalut.DIA;
            default:
                throw new ReportGenerationException(Salut.class, null, null, "Unknown agrupacio value: " + agrupacio);
        }
    }

    /**
     * Aplica un petit marge
     */
    private LocalDateTime calculaDataIniciAmbMarge(LocalDateTime dataInici, TipusRegistreSalut tipus) {
//        TODO Provar si realment és necessari aquest marge per DIA
        if (tipus == TipusRegistreSalut.DIA) {
            return dataInici.plusMinutes(60);
        }
        return dataInici;
    }

    /**
     * Genera una llista d'objectes SalutInformeEstatItem basats en els paràmetres proporcionats.
     *
     * @param dataInici la data i hora d'inici utilitzades per filtrar les entitats
     * @param tipus el tipus de registre utilitzat per filtrar les entitats
     * @param entornAppId l'ID de l'entorn d'aplicació utilitzat per filtrar les entitats
     * @return una llista d'objectes SalutInformeEstatItem creats a partir de les entitats filtrades
     */
    private List<SalutInformeEstatItem> generateSalutEstatList(LocalDateTime dataInici, TipusRegistreSalut tipus, Long entornAppId) {
        List<SalutEntity> salutEntityList = ((SalutRepository) entityRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                entornAppId,
                dataInici,
                tipus);
        return salutEntityList.stream().map(SalutInformeEstatItem::new).collect(Collectors.toList());
    }

    /**
     * Comprova si una franja és vàlida per a l'agrupació de MINUTS
     * usant la constant MINUTS_PER_AGRUPACIO
     */
    private boolean isFranjaMinutsInvalida(LocalDateTime dataInici, LocalDateTime dataFi) {
        int dataIniciMinutesMod = dataInici.getMinute() % MINUTS_PER_AGRUPACIO;
        int dataFiMinutesMod = dataFi.getMinute() % MINUTS_PER_AGRUPACIO;
        return dataIniciMinutesMod != 0 || dataFiMinutesMod != 0;
    }
}
