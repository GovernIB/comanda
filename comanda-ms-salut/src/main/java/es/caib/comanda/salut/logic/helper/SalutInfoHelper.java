package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.model.v1.salut.DetallSalut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.model.v1.salut.InformacioSistema;
import es.caib.comanda.model.v1.salut.IntegracioPeticions;
import es.caib.comanda.model.v1.salut.IntegracioSalut;
import es.caib.comanda.model.v1.salut.MissatgeSalut;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.model.v1.salut.SubsistemaSalut;
import es.caib.comanda.salut.logic.event.SalutCompactionFinishedEvent;
import es.caib.comanda.salut.logic.event.SalutInfoUpdatedEvent;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Lògica comuna per a consultar la informació de salut de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalutInfoHelper {

	private final SalutRepository salutRepository;
	private final SalutIntegracioRepository salutIntegracioRepository;
	private final SalutSubsistemaRepository salutSubsistemaRepository;
	private final SalutMissatgeRepository salutMissatgeRepository;
	private final SalutDetallRepository salutDetallRepository;

	private final SalutClientHelper salutClientHelper;
	private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final MetricsHelper metricsHelper;
    private final SalutPurgeHelper salutPurgeService;

	// Locks per assegurar compactació "synchronized" per entornAppId
	private static final ConcurrentHashMap<Long, Object> ENTORN_LOCKS = new ConcurrentHashMap<>();

	@Transactional
	public void getSalutInfo(EntornApp entornApp) {
		log.debug("Obtenint dades de salut de l'app {}, entorn {}",
				entornApp.getApp().getNom(),
				entornApp.getEntorn().getNom());
		Instant t0 = Instant.now();
		MonitorSalut monitorSalut = new MonitorSalut(
				entornApp.getId(),
				entornApp.getSalutUrl(),
				salutClientHelper);
		LocalDateTime currentMinuteTime = LocalDateTime.now().withSecond(0).withNano(0);
		Long idSalut = null;
		try {
            // Obtenir dades de salut de l'aplicació
            monitorSalut.startAction();
            // Validació prèvia de la URL per evitar IllegalArgumentException i errors no controlats
            URI uri = buildUriOrNull(entornApp.getSalutUrl());
            if (!isValidUri(uri)) {
                idSalut = processSalutError(entornApp, idSalut, currentMinuteTime, monitorSalut, new MalformedURLException("URL de salut invàlida o no absoluta"));
                return; // sortir; el bloc finally publicarà l'event i mètriques
            }
			SalutInfo salutInfo = restTemplate.getForObject(uri, SalutInfo.class);
			monitorSalut.endAction();
			// Guardar les dades de salut a la base de dades
			idSalut = crearSalut(salutInfo, entornApp.getId(), currentMinuteTime);
		} catch (RestClientException ex) {
            idSalut = processSalutError(entornApp, idSalut, currentMinuteTime, monitorSalut, ex);
        } finally {
			Duration duration = Duration.between(t0, Instant.now());
			metricsHelper.getSalutInfoGlobalTimer(null, null).record(duration);
			metricsHelper.getSalutInfoGlobalTimer(
					entornApp.getEntorn().getNom(),
					entornApp.getApp().getNom()).record(duration);
			// Publicar esdeveniment per a compactació. També en cas d'error
			eventPublisher.publishEvent(new SalutInfoUpdatedEvent(entornApp.getId(), idSalut));
		}
	}

    private Long processSalutError(EntornApp entornApp, Long idSalut, LocalDateTime currentMinuteTime, MonitorSalut monitorSalut, Exception ex) {
        log.warn("No s'han pogut obtenir dades de salut de l'app {}, entorn {}: {}",
                entornApp.getApp().getNom(),
                entornApp.getEntorn().getNom(),
                ex.getLocalizedMessage());

        SalutEntity salut = new SalutEntity();
        salut.setEntornAppId(entornApp.getId());
        salut.setData(currentMinuteTime);
        salut.setAppEstat(SalutEstat.DOWN);
        salut.updateAppCountByEstat(SalutEstat.DOWN);
        salut.setPeticioError(true);
        salut.setNumElements(1);
        SalutEntity saved = salutRepository.save(salut);
        if (!monitorSalut.isFinishedAction()) {
            monitorSalut.endAction(ex);
        }
        idSalut = saved.getId();
        return idSalut;
    }

    private boolean isValidUri(URI uri) {
        return (uri != null && uri.isAbsolute());
    }

    private URI buildUriOrNull(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        try {
            URI uri = URI.create(trimmed);
            return uri;
        } catch (Exception ex) {
            return null;
        }
    }

	private Long crearSalut(SalutInfo info, Long entornAppId, LocalDateTime currentMinuteTime) {
		if (info != null) {
			// Si es desconeix l'estatGlobal de l'aplicació, entenem que està DOWN
			SalutEntity salut = new SalutEntity();
			salut.setEntornAppId(entornAppId);
			salut.setData(currentMinuteTime);
			salut.setDataApp(toLocalDateTime(info.getData()));
			salut.setTipusRegistre(TipusRegistreSalut.MINUT);
			SalutEstat appEstat = toSalutEstat(info.getEstatGlobal().getEstat());
			salut.setAppEstat(appEstat);
			salut.updateAppCountByEstat(appEstat);
			SalutEstat bdEstat = toSalutEstat(info.getEstatBaseDeDades().getEstat());
			salut.setBdEstat(bdEstat);
			salut.updateBdCountByEstat(bdEstat);
			salut.setAppLatencia(info.getEstatGlobal().getLatencia());
			salut.setAppLatenciaMitjana(info.getEstatGlobal().getLatencia());
			salut.setBdLatencia(info.getEstatBaseDeDades().getLatencia());
			salut.setBdLatenciaMitjana(info.getEstatBaseDeDades().getLatencia());
			salut.setNumElements(1);
			SalutEntity saved = salutRepository.save(salut);
			crearSalutIntegracions(saved, info.getIntegracions());
			crearSalutSubsistemes(saved, info.getSubsistemes());
            crearSalutMissatges(saved, info.getMissatges());
            crearSalutDetalls(saved, toDetallSalutList(info.getInformacioSistema()));
            return saved.getId();
        }
        return null;
    }

    private Set<ConstraintViolation<Object>> validateObject(Object object) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(object);
        }
    }

    /**
     * Construeix la llista de DetallSalut a partir del nou objecte InformacioSistema per a persistència històrica.
     * Codis utilitzats (compatibilitat amb format antic):
     *  PRC (processadors), LAVG (càrrega), SCPU (CPU sistema), MET (memòria total), MED (memòria disponible),
     *  EDT (disc total), EDL (disc lliure), SO (sistema operatiu), ST (data arrencada), UT (uptime).
     */
    private List<DetallSalut> toDetallSalutList(InformacioSistema info) {
        java.util.ArrayList<DetallSalut> list = new java.util.ArrayList<>();
        if (info == null) return list;
        if (info.getProcessadors() != null) list.add(DetallSalut.builder().codi("PRC").nom("Processadors").valor(String.valueOf(info.getProcessadors())).build());
        if (info.getCarregaSistema() != null) list.add(DetallSalut.builder().codi("LAVG").nom("Càrrega del sistema (LoadAvg)").valor(info.getCarregaSistema()).build());
        if (info.getCpuSistema() != null) list.add(DetallSalut.builder().codi("SCPU").nom("CPU sistema").valor(info.getCpuSistema()).build());
        if (info.getMemoriaTotal() != null) list.add(DetallSalut.builder().codi("MET").nom("Memòria total").valor(info.getMemoriaTotal()).build());
        if (info.getMemoriaDisponible() != null) list.add(DetallSalut.builder().codi("MED").nom("Memòria disponible").valor(info.getMemoriaDisponible()).build());
        if (info.getEspaiDiscTotal() != null) list.add(DetallSalut.builder().codi("EDT").nom("Espai de disc total").valor(info.getEspaiDiscTotal()).build());
        if (info.getEspaiDiscLliure() != null) list.add(DetallSalut.builder().codi("EDL").nom("Espai de disc lliure").valor(info.getEspaiDiscLliure()).build());
        if (info.getSistemaOperatiu() != null) list.add(DetallSalut.builder().codi("SO").nom("Sistema operatiu").valor(info.getSistemaOperatiu()).build());
        if (info.getDataArrencada() != null) list.add(DetallSalut.builder().codi("ST").nom("Data arrancada").valor(info.getDataArrencada()).build());
        if (info.getTempsFuncionant() != null) list.add(DetallSalut.builder().codi("UT").nom("Temps funcionant").valor(info.getTempsFuncionant()).build());
        return list;
    }

	private void crearSalutIntegracions(
			SalutEntity salut,
			Collection<IntegracioSalut> integracions) {
        List<IntegracioSalut> filteredIntegracions = integracions != null ? integracions.stream()
                .filter(i -> {
                    var violations = validateObject(i);
                     if (!violations.isEmpty()) {
                         if (violations.size() == 1 && violations.stream().anyMatch(v -> "peticions.peticionsPerEntornSenseClausNulles".equals(v.getPropertyPath().toString()))) {
                            log.warn("SalutIntegracio {} (salut: {}) peticionsPerEntorn amb claus buides!: {}", i.getCodi(), salut.getId(), violations);
                            return true;
                         }
                         log.warn("SalutIntegracio {} (salut: {}) no validat: {}", i.getCodi(), salut.getId(), violations);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList()) : null;

		if (filteredIntegracions != null) {
			filteredIntegracions.forEach(i -> {
				SalutIntegracioEntity salutIntegracio = new SalutIntegracioEntity();
				salutIntegracio.setCodi(i.getCodi());
                salutIntegracio.setEstat(toSalutEstat(i.getEstat()));
				salutIntegracio.setLatencia(i.getLatencia());
                salutIntegracio.setLatenciaMitjana(i.getLatencia());
				salutIntegracio.setTotalOk(i.getPeticions() != null ? i.getPeticions().getTotalOk() : 0L);
				salutIntegracio.setTotalError(i.getPeticions() != null ? i.getPeticions().getTotalError() : 0L);
				salutIntegracio.setTotalTempsMig(i.getPeticions() != null && i.getPeticions().getTotalTempsMig() != null ? i.getPeticions().getTotalTempsMig() : 0);
				salutIntegracio.setPeticionsOkUltimPeriode(i.getPeticions() != null && i.getPeticions().getPeticionsOkUltimPeriode() != null ? i.getPeticions().getPeticionsOkUltimPeriode() : 0L);
				salutIntegracio.setPeticionsErrorUltimPeriode(i.getPeticions() != null && i.getPeticions().getPeticionsErrorUltimPeriode() != null ? i.getPeticions().getPeticionsErrorUltimPeriode() : 0L);
				salutIntegracio.setTempsMigUltimPeriode(i.getPeticions() != null && i.getPeticions().getTempsMigUltimPeriode() != null ? i.getPeticions().getTempsMigUltimPeriode() : 0);
				salutIntegracio.setEndpoint(i.getPeticions() != null ? i.getPeticions().getEndpoint() : null);
				salutIntegracio.setSalut(salut);
				SalutIntegracioEntity salutIntegracioSaved = salutIntegracioRepository.save(salutIntegracio);
				if (i.getPeticions() != null && i.getPeticions().getPeticionsPerEntorn() != null) {
                    i.getPeticions().getPeticionsPerEntorn().keySet().forEach(peticioEntornKey -> {
                        IntegracioPeticions peticioEntorn = i.getPeticions().getPeticionsPerEntorn().get(peticioEntornKey);
                        SalutIntegracioEntity salutIntegracioFilla = new SalutIntegracioEntity();
                        salutIntegracioFilla.setCodi((!Strings.isBlank(peticioEntornKey)) ? StringUtils.truncate(peticioEntornKey, SalutIntegracioEntity.CODI_MAX_LENGTH) : "--");
                        salutIntegracioFilla.setEstat(toSalutEstat(i.getEstat()));
                        salutIntegracioFilla.setTotalOk(peticioEntorn.getTotalOk());
                        salutIntegracioFilla.setTotalError(peticioEntorn.getTotalError());
                        salutIntegracioFilla.setTotalTempsMig(peticioEntorn.getTotalTempsMig());
                        salutIntegracioFilla.setPeticionsOkUltimPeriode(peticioEntorn.getPeticionsOkUltimPeriode());
                        salutIntegracioFilla.setPeticionsErrorUltimPeriode(peticioEntorn.getPeticionsErrorUltimPeriode());
                        salutIntegracioFilla.setTempsMigUltimPeriode(peticioEntorn.getTempsMigUltimPeriode());
                        salutIntegracioFilla.setEndpoint(peticioEntorn.getEndpoint());
                        salutIntegracioFilla.setSalut(salut);
                        salutIntegracioFilla.setPare(salutIntegracioSaved);
                        salutIntegracioRepository.save(salutIntegracioFilla);
                    });
				}
			});
		}
	}

	private void crearSalutSubsistemes(SalutEntity salut, List<SubsistemaSalut> subsistemes) {
        List<SubsistemaSalut> filteredSubsistemes = subsistemes != null ? subsistemes.stream()
                .filter(s -> {
                    var violations = validateObject(s);
                    if (!violations.isEmpty()) {
                        log.warn("SalutSubsistema {} (salut: {}) no validat: {}", s.getCodi(), salut.getId(), violations);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList()) : null;

		if (filteredSubsistemes != null) {
			filteredSubsistemes.forEach(s -> {
				SalutSubsistemaEntity salutSubsistema = new SalutSubsistemaEntity();
				salutSubsistema.setCodi(s.getCodi());
				salutSubsistema.setEstat(toSalutEstat(s.getEstat()));
				salutSubsistema.setLatencia(s.getLatencia());
                salutSubsistema.setLatenciaMitjana(s.getLatencia());
				salutSubsistema.setTotalOk(s.getTotalOk() != null ? s.getTotalOk() : 0L);
				salutSubsistema.setTotalError(s.getTotalError() != null ? s.getTotalError() : 0L);
				salutSubsistema.setTotalTempsMig(s.getTotalTempsMig() != null ? s.getTotalTempsMig() : 0);
				salutSubsistema.setPeticionsOkUltimPeriode(s.getPeticionsOkUltimPeriode() != null ? s.getPeticionsOkUltimPeriode() : 0L);
				salutSubsistema.setPeticionsErrorUltimPeriode(s.getPeticionsErrorUltimPeriode() != null ? s.getPeticionsErrorUltimPeriode() : 0L);
				salutSubsistema.setTempsMigUltimPeriode(s.getTempsMigUltimPeriode() != null ? s.getTempsMigUltimPeriode() : 0);
				salutSubsistema.setSalut(salut);
				salutSubsistemaRepository.save(salutSubsistema);
			});
		}
	}

	private void crearSalutMissatges(SalutEntity salut, List<MissatgeSalut> missatges) {
        List<MissatgeSalut> filteredMissatges = missatges != null ? missatges.stream()
                .filter(m -> {
                    var violations = validateObject(m);
                    if (!violations.isEmpty()) {
                        log.warn("SalutMissatge amb data {} (salut: {}) no validat: {}", m.getData(), salut.getId(), violations);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList()) : null;

		if (filteredMissatges != null) {
            filteredMissatges.forEach(m -> {
				SalutMissatgeEntity salutMissatge = new SalutMissatgeEntity();
				salutMissatge.setData(toLocalDateTime(m.getData()));
				salutMissatge.setNivell(m.getNivell());
				salutMissatge.setMissatge(m.getMissatge());
				salutMissatge.setSalut(salut);
				salutMissatgeRepository.save(salutMissatge);
			});
		}
	}

	private void crearSalutDetalls(SalutEntity salut, List<DetallSalut> detalls) {
        List<DetallSalut> filteredDetalls = detalls != null ? detalls.stream()
                .filter(d -> {
                    var violations = validateObject(d);
                    if (!violations.isEmpty()) {
                        log.warn("SalutDetalls {} (salut: {}) no validat: {}", d.getCodi(), salut.getId(), violations);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList()) : null;

		if (filteredDetalls != null) {
			filteredDetalls.forEach(d -> {
				SalutDetallEntity salutDetall = new SalutDetallEntity();
				salutDetall.setCodi(d.getCodi());
				salutDetall.setNom(d.getNom());
				salutDetall.setValor(d.getValor());
				salutDetall.setSalut(salut);
				salutDetallRepository.save(salutDetall);
			});
		}
	}

	private SalutEstat toSalutEstat(EstatSalutEnum estatSalut) {
		if (estatSalut == null) return SalutEstat.UNKNOWN;
		try {
			return SalutEstat.valueOf(estatSalut.name());
		} catch (IllegalArgumentException ignored) {}
		return SalutEstat.UNKNOWN;
	}

	public LocalDateTime toLocalDateTime(Date dateToConvert) {
		return dateToConvert != null ? LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault()) : null;
	}


    // Compactat de dades de salut
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final int MINUTS_PER_AGRUPACIO = 4;

    @Transactional
    public void compactar(Long entornAppId, Long salutId) {

        Object lock = ENTORN_LOCKS.computeIfAbsent(entornAppId, k -> new Object());
        synchronized (lock) {
            try {
                log.info("Executant compactat de dades de salut. EntornAppId: {}, salutId: {}.",
                        entornAppId, salutId);
                SalutEntity dadesSalut = salutRepository.findById(salutId).orElse(null);
                if (dadesSalut == null) {
                    log.warn("No s'ha trobat el registre de salut amb id {} per a l'entorn {}. Possiblement la transacció encara no està confirmada.", salutId, entornAppId);
                    return;
                }
                // Crear agregats per grups de minuts, hores i dies
                agregarGrupMinuts(entornAppId, dadesSalut);
                agregarHora(entornAppId, dadesSalut);
                agregarDia(entornAppId, dadesSalut);

                // Publicar esdeveniment per a compactació. També en cas d'error
                eventPublisher.publishEvent(new SalutCompactionFinishedEvent(entornAppId, salutId));
            } catch (Exception e) {
                log.error("Error durant el procés de compactat de salut. EntornAppId: {}, salutId: {}", entornAppId, salutId, e);
                throw e;
            }
        }
    }

    public void buidar(Long entornAppId, Long salutId) {
        try {
            log.info("Executant buidat de dades de salut. EntornAppId: {}, salutId: {}.",
                    entornAppId, salutId);

            SalutEntity dadesSalut = salutRepository.findById(salutId).orElse(null);
            if (dadesSalut == null) {
                log.warn("No s'ha trobat el registre de salut amb id {} per a l'entorn {}. Possiblement la transacció encara no està confirmada.", salutId, entornAppId);
                return;
            }

            // Eliminar registres de minuts més antics que 15 minuts (+1 minut de marge)
            LocalDateTime menys15m = dadesSalut.getData().minusMinutes(16);
            eliminarAntigues(entornAppId, TipusRegistreSalut.MINUT, menys15m);

            // Eliminar registres de grups de minuts més antics que 1 hora i (MINUTS_PER_AGRUPACIO) minuts
            LocalDateTime menys1h = dadesSalut.getData().minusHours(1).minusMinutes(MINUTS_PER_AGRUPACIO);
            eliminarAntigues(entornAppId, TipusRegistreSalut.MINUTS, menys1h);

            // Eliminar registres de hores més antics que 1 dia
            LocalDateTime menys1d = dadesSalut.getData().minusDays(1).minusMinutes(59);
            eliminarAntigues(entornAppId, TipusRegistreSalut.HORA, menys1d);

            // Eliminar rgistres de dies més antics que 1 mes
            LocalDateTime menys1M = dadesSalut.getData().minusMonths(1).minusHours(23).minusMinutes(59);
            eliminarAntigues(entornAppId, TipusRegistreSalut.DIA, menys1M);
        } catch (Exception e) {
            log.error("Error durant el procés de buidat de salut. EntornAppId: {}, salutId: {}", entornAppId, salutId, e);
            throw e;
        }
    }

    private void eliminarAntigues(Long entornAppId, TipusRegistreSalut tipus, LocalDateTime dataLlindar) {
        try {
            salutPurgeService.eliminarDadesSalutAntigues(entornAppId, tipus, dataLlindar);
        } catch (Exception ex) {
            log.warn("Error eliminant dades antigues ({}). Es continuarà sense rollback del buidat/compactat. entornAppId={}, data={} -> {}", tipus.name(), entornAppId, dataLlindar, ex.getMessage(), ex);
        }
    }

    // Expose constant for tests maintaining backward compatibility
    public static final int PURGE_BATCH_SIZE = SalutPurgeHelper.PURGE_BATCH_SIZE;

    // Backward-compatible delegation method for tests/clients
    public void eliminarDadesSalutAntigues(Long entornAppId, es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut tipus, LocalDateTime data) {
        salutPurgeService.eliminarDadesSalutAntigues(entornAppId, tipus, data);
    }

    private void agregarGrupMinuts(Long entornAppId, SalutEntity darreraActualitzacioSalut) {
        if (darreraActualitzacioSalut == null) return;

        SalutEntity salutMinuts = salutRepository.findTopByEntornAppIdAndTipusRegistreOrderByIdDesc(entornAppId, TipusRegistreSalut.MINUTS);
        if (salutMinuts != null && darreraActualitzacioSalut.getData().isBefore(salutMinuts.getData())) {
            log.error("Informació de salut anterior a l'agrupació de minuts");
            return;
        }

        // Comprova que la data sigui dins els MINUTS_PER_AGRUPACIO (inclosos) a partir de l'inici de l'agrupada
        // Exemple: si MINUTS_PER_AGRUPACIO=4 -> finestra inclusiva de 0..3 minuts; el 5è registre crea un nou agregat
        if (salutMinuts != null && !darreraActualitzacioSalut.getData().isAfter(salutMinuts.getData().plusMinutes(MINUTS_PER_AGRUPACIO - 1))) {
            afegirSalut(salutMinuts, darreraActualitzacioSalut);
        } else {
            creaAgregatSalut(entornAppId, TipusRegistreSalut.MINUTS, darreraActualitzacioSalut);
        }
    }

    private void agregarHora(Long entornAppId, SalutEntity darreraActualitzacioSalut) {
        if (darreraActualitzacioSalut == null) return;

        if (isFirstMinuteOfHour(darreraActualitzacioSalut.getData())) {
            creaAgregatSalut(entornAppId, TipusRegistreSalut.HORA, darreraActualitzacioSalut);
        } else {
            SalutEntity salutHora = salutRepository.findTopByEntornAppIdAndTipusRegistreOrderByIdDesc(entornAppId, TipusRegistreSalut.HORA);
            if (salutHora != null && darreraActualitzacioSalut.getData().isBefore(salutHora.getData())) {
                log.error("Informació de salut anterior a l'agrupació de minuts");
                return;
            }
            // Comprova que la data sigui dins els 4 minuts posteriors a l'inici de l'agrupada
            if (salutHora != null && darreraActualitzacioSalut.getData().getHour() == salutHora.getData().getHour()) {
                afegirSalut(salutHora, darreraActualitzacioSalut);
            } else {
                creaAgregatSalut(entornAppId, TipusRegistreSalut.HORA, darreraActualitzacioSalut);
            }
        }
    }

    private void agregarDia(Long entornAppId, SalutEntity darreraActualitzacioSalut) {
        if (darreraActualitzacioSalut == null) return;

        if (isFirstMinuteOfDay(darreraActualitzacioSalut.getData())) {
            creaAgregatSalut(entornAppId, TipusRegistreSalut.DIA, darreraActualitzacioSalut);
        } else {
            SalutEntity salutDia = salutRepository.findTopByEntornAppIdAndTipusRegistreOrderByIdDesc(entornAppId, TipusRegistreSalut.DIA);
            if (salutDia != null && darreraActualitzacioSalut.getData().isBefore(salutDia.getData())) {
                log.error("Informació de salut anterior a l'agrupació de minuts");
                return;
            }
            // Comprova que la data sigui del dia de l'agrupada
            if (salutDia != null && darreraActualitzacioSalut.getData().getDayOfMonth() == salutDia.getData().getDayOfMonth()) {
                afegirSalut(salutDia, darreraActualitzacioSalut);
            } else {
                creaAgregatSalut(entornAppId, TipusRegistreSalut.DIA, darreraActualitzacioSalut);
            }
        }
    }

    // Purga delegada al servei dedicat

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Crea un nou registre SalutEntity agregat a partir d'un únic registre de tipus MINUT.
     * No elimina el registre original.
     */
    public SalutEntity creaAgregatSalut(Long entornAppId, TipusRegistreSalut tipus, SalutEntity salut) {
        if (salut == null) return null;

        
        SalutEntity agregat = new SalutEntity();
        agregat.setEntornAppId(entornAppId);
        LocalDateTime data = salut.getData();
        if (tipus == TipusRegistreSalut.MINUTS) {
            int dataMinutesModulus = data.getMinute() % MINUTS_PER_AGRUPACIO;
            data = dataMinutesModulus != 0 ?
                    data.minusMinutes(dataMinutesModulus).withSecond(0).withNano(0)
                    : data.withSecond(0).withNano(0);
        } else if (tipus == TipusRegistreSalut.HORA) {
            data = data.withMinute(0).withSecond(0).withNano(0);
        } else if (tipus == TipusRegistreSalut.DIA) {
            data = data.withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        agregat.setData(data);
        agregat.setTipusRegistre(tipus);

        agregat.setAppEstat(salut.getAppEstat());
        agregat.updateAppCountByEstat(salut.getAppEstat());
        agregat.setBdEstat(salut.getBdEstat());
        agregat.updateBdCountByEstat(salut.getBdEstat());

        agregat.setAppLatencia(salut.getAppLatencia());
        agregat.setAppLatenciaMitjana(salut.getAppLatencia());
        agregat.setBdLatencia(salut.getBdLatencia());
        agregat.setBdLatenciaMitjana(salut.getBdLatencia());
        agregat.setNumElements(1);
        agregat = salutRepository.save(agregat);

        crearIntegracions(agregat, salut);
        crearSubsistemes(agregat, salut);
        crearMissatges(agregat, salut);
        crearDetalls(agregat, salut);
        return agregat;
    }

    /**
     * Afegeix la informació d'un registre de tipus MINUT a un SalutEntity agregat existent.
     * Implementació mínima: actualitza data, pitjor estat i mitjanes bàsiques; copia darrer missatge/detalls.
     */
    public SalutEntity afegirSalut(SalutEntity agregat, SalutEntity salut) {
        if (salut == null) return null;

        TipusRegistreSalut tipus = agregat.getTipusRegistre();

        // Mantenim el darrer valor de l'estat de App i BBDD
        agregat.setAppEstat(salut.getAppEstat());
        agregat.updateAppCountByEstat(salut.getAppEstat());
        agregat.setBdEstat(salut.getBdEstat());
        agregat.updateBdCountByEstat(salut.getBdEstat());

        agregat.setAppLatencia(salut.getAppLatencia());
        agregat.addAppLatenciaMitjana(salut.getAppLatencia());
        agregat.setBdLatencia(salut.getBdLatencia());
        agregat.addBdLatenciaMitjana(salut.getAppLatencia());

        agregat.setNumElements(agregat.getNumElements() + 1);

        afegirIntegracions(agregat, salut);
        afegirSubsistemes(agregat, salut);
        afegirMissatges(agregat, salut);
        afegirDetalls(agregat, salut);
        return agregat;
    }

    private void crearIntegracions(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutIntegracions() == null || salut.getSalutIntegracions().isEmpty()) return;

        salut.getSalutIntegracions().forEach(si -> {
            crearIntegracio(agregat, si);
        });
    }

    private void crearIntegracio(SalutEntity agregat, SalutIntegracioEntity si) {
        SalutIntegracioEntity salutIntegracio = new SalutIntegracioEntity();
	    salutIntegracio.setSalut(agregat);
	    salutIntegracio.setCodi(si.getCodi());
	    salutIntegracio.setTotalOk(si.getTotalOk() != null ? si.getTotalOk() : 0L);
	    salutIntegracio.setTotalError(si.getTotalError() != null ? si.getTotalError() : 0L);
	    salutIntegracio.setTotalTempsMig(si.getTotalTempsMig() != null ? si.getTotalTempsMig() : 0);
	    salutIntegracio.setPeticionsOkUltimPeriode(si.getPeticionsOkUltimPeriode() != null ? si.getPeticionsOkUltimPeriode() : 0L);
	    salutIntegracio.setPeticionsErrorUltimPeriode(si.getPeticionsErrorUltimPeriode() != null ? si.getPeticionsErrorUltimPeriode() : 0L);
	    salutIntegracio.setTempsMigUltimPeriode(si.getTempsMigUltimPeriode() != null ? si.getTempsMigUltimPeriode() : 0);
	    if (si.getLatencia() != null) {
		    salutIntegracio.setLatencia(si.getLatencia());
		    salutIntegracio.setLatenciaMitjana(si.getLatencia());
	    }
        if (si.getEstat() == null) {
            si.setEstat(SalutEstat.UNKNOWN);
        }
	    salutIntegracio.setEstat(si.getEstat());
	    salutIntegracio.updateCountByEstat(si.getEstat());
        salutIntegracioRepository.save(salutIntegracio);
    }

    private void afegirIntegracions(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutIntegracions() == null || salut.getSalutIntegracions().isEmpty()) return;

        salut.getSalutIntegracions().forEach(si -> {
            agregat.getSalutIntegracions().stream()
                    .filter(a -> a.getCodi().equals(si.getCodi())).findFirst()
                    .ifPresentOrElse(integracio -> {
                        integracio.setTotalOk(si.getTotalOk());
                        integracio.setTotalError(si.getTotalError());
                        integracio.setTotalTempsMig(si.getTotalTempsMig());
                        integracio.addTempsMigUltimPeriode(si.getTempsMigUltimPeriode(), si.getPeticionsOkUltimPeriode());
                        integracio.addPeticionsOkUltimPeriode(si.getPeticionsOkUltimPeriode());
                        integracio.addPeticionsErrorUltimPeriode(si.getPeticionsErrorUltimPeriode());
                        if (si.getLatencia() != null) {
                            integracio.setLatencia(si.getLatencia());
                            integracio.addLatenciaMitjana(si.getLatencia());
                        }
                        if (si.getEstat() != null) {
                            integracio.setEstat(si.getEstat());
                            integracio.updateCountByEstat(si.getEstat());
                        }
                        salutIntegracioRepository.save(integracio);
                    }, () -> {
                        crearIntegracio(agregat, si);
                    });
        });
    }

    private void crearSubsistemes(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutSubsistemes() == null || salut.getSalutSubsistemes().isEmpty()) return;

        salut.getSalutSubsistemes().forEach(ss -> {
            crearSubsistema(agregat, ss);
        });
    }

    private void crearSubsistema(SalutEntity agregat, SalutSubsistemaEntity ss) {
        SalutSubsistemaEntity subsistema = new SalutSubsistemaEntity();
        subsistema.setSalut(agregat);
        subsistema.setCodi(ss.getCodi());
        subsistema.setTotalOk(ss.getTotalOk());
        subsistema.setTotalError(ss.getTotalError());
	    subsistema.setTotalTempsMig(ss.getTotalTempsMig());
	    subsistema.setPeticionsOkUltimPeriode(ss.getPeticionsOkUltimPeriode());
	    subsistema.setPeticionsErrorUltimPeriode(ss.getPeticionsErrorUltimPeriode());
	    subsistema.setTempsMigUltimPeriode(ss.getTempsMigUltimPeriode());
        if (ss.getLatencia() != null) {
            subsistema.setLatencia(ss.getLatencia());
            subsistema.setLatenciaMitjana(ss.getLatencia());
        }
        if (ss.getEstat() == null) {
            ss.setEstat(SalutEstat.UNKNOWN);
        }
        subsistema.setEstat(ss.getEstat());
        subsistema.updateCountByEstat(ss.getEstat());
        salutSubsistemaRepository.save(subsistema);
    }

    private void afegirSubsistemes(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutSubsistemes() == null || salut.getSalutSubsistemes().isEmpty()) return;

        salut.getSalutSubsistemes().forEach(ss -> {
            agregat.getSalutSubsistemes().stream()
                    .filter(a -> a.getCodi().equals(ss.getCodi())).findFirst()
                    .ifPresentOrElse(subsistema -> {
                        subsistema.setTotalOk(ss.getTotalOk());
                        subsistema.setTotalError(ss.getTotalError());
                        subsistema.setTotalTempsMig(ss.getTotalTempsMig());
                        subsistema.addTempsMigUltimPeriode(ss.getTempsMigUltimPeriode(), ss.getPeticionsOkUltimPeriode());
                        subsistema.addPeticionsOkUltimPeriode(ss.getPeticionsOkUltimPeriode());
                        subsistema.addPeticionsErrorUltimPeriode(ss.getPeticionsErrorUltimPeriode());
                        if (ss.getLatencia() != null) {
                            subsistema.setLatencia(ss.getLatencia());
                            subsistema.addLatenciaMitjana(ss.getLatencia());
                        }
                        if (ss.getEstat() != null) {
                            subsistema.setEstat(ss.getEstat());
                            subsistema.updateCountByEstat(ss.getEstat());
                        }
                        salutSubsistemaRepository.save(subsistema);
                    }, () -> {
                        crearSubsistema(agregat, ss);
                    });
        });
    }

    private void crearMissatges(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutMissatges() == null || salut.getSalutMissatges().isEmpty()) return;

        salut.getSalutMissatges().forEach(sm -> {
            SalutMissatgeEntity missatge = new SalutMissatgeEntity();
            missatge.setSalut(agregat);
            missatge.setData(sm.getData());
            missatge.setNivell(sm.getNivell());
            missatge.setMissatge(sm.getMissatge());
            salutMissatgeRepository.save(missatge);
        });
    }

    private void afegirMissatges(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutMissatges() == null || salut.getSalutMissatges().isEmpty()) return;

        salut.getSalutMissatges().forEach(sm -> {
            agregat.getSalutMissatges().stream()
                    .filter(missatge -> missatge.getMissatge().equals(sm.getMissatge())).findFirst()
                    .ifPresentOrElse(missatge -> {
                        missatge.setNivell(sm.getNivell());
                        salutMissatgeRepository.save(missatge);
                    }, () -> {
                        SalutMissatgeEntity missatge = new SalutMissatgeEntity();
                        missatge.setSalut(agregat);
                        missatge.setData(sm.getData());
                        missatge.setNivell(sm.getNivell());
                        missatge.setMissatge(sm.getMissatge());
                        salutMissatgeRepository.save(missatge);
                    });
        });
    }

    private void crearDetalls(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutDetalls() == null || salut.getSalutDetalls().isEmpty()) return;

        salut.getSalutDetalls().forEach(sd -> {
            SalutDetallEntity detall = new SalutDetallEntity();
            detall.setSalut(agregat);
            detall.setCodi(sd.getCodi());
            detall.setNom(sd.getNom());
            detall.setValor(sd.getValor());
            salutDetallRepository.save(detall);
        });
    }

    private void afegirDetalls(SalutEntity agregat, SalutEntity salut) {
        if (salut.getSalutDetalls() == null || salut.getSalutDetalls().isEmpty()) return;

        salut.getSalutDetalls().forEach(sd -> {
            agregat.getSalutDetalls().stream()
                    .filter(a -> a.getCodi().equals(sd.getCodi())).findFirst()
                    .ifPresentOrElse(detall -> {
                        detall.setNom(sd.getNom());
                        detall.setValor(sd.getValor());
                        salutDetallRepository.save(detall);
                    }, () -> {
                        SalutDetallEntity detall = new SalutDetallEntity();
                        detall.setSalut(agregat);
                        detall.setCodi(sd.getCodi());
                        detall.setNom(sd.getNom());
                        detall.setValor(sd.getValor());
                        salutDetallRepository.save(detall);
                    });
        });
    }

    private boolean isFirstMinuteOfHour(LocalDateTime dateTime) {
        return dateTime != null && dateTime.getMinute() == 0;
    }

    private boolean isFirstMinuteOfDay(LocalDateTime dateTime) {
        return dateTime != null && dateTime.getHour() == 0 && dateTime.getMinute() == 0;
    }


}


