package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.salut.model.DetallSalut;
import es.caib.comanda.ms.salut.model.EstatSalutEnum;
import es.caib.comanda.ms.salut.model.IntegracioSalut;
import es.caib.comanda.ms.salut.model.MissatgeSalut;
import es.caib.comanda.ms.salut.model.SalutInfo;
import es.caib.comanda.ms.salut.model.SubsistemaSalut;
import es.caib.comanda.salut.logic.event.SalutInfoUpdatedEvent;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutNivell;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.LockTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

	@Lazy
	private final SalutInfoHelper self = this;

	// Locks per assegurar compactació "synchronized" per entornAppId
	private static final java.util.concurrent.ConcurrentHashMap<Long, Object> ENTORN_LOCKS = new java.util.concurrent.ConcurrentHashMap<>();
	private static final Integer BATCH_SIZE = 500;

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
		int numeroDiesAgrupacio = actualitzaInfoCompactacio(entornApp.getId());
		Long idSalut = null;
		try {
			// Obtenir dades de salut de l'aplicació
			monitorSalut.startAction();
			SalutInfo salutInfo = restTemplate.getForObject(entornApp.getSalutUrl(), SalutInfo.class);
			monitorSalut.endAction();
			// Guardar les dades de salut a la base de dades
			idSalut = crearSalut(salutInfo, entornApp.getId(), currentMinuteTime);
		} catch (RestClientException ex) {
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
		} finally {
			Duration duration = Duration.between(t0, Instant.now());
			metricsHelper.getSalutInfoGlobalTimer(null, null).record(duration);
			metricsHelper.getSalutInfoGlobalTimer(
					entornApp.getEntorn().getNom(),
					entornApp.getApp().getNom()).record(duration);
			// Publicar esdeveniment per a compactació. També en cas d'error
			eventPublisher.publishEvent(new SalutInfoUpdatedEvent(entornApp.getId(), idSalut, numeroDiesAgrupacio));
		}
	}

	private Long crearSalut(SalutInfo info, Long entornAppId, LocalDateTime currentMinuteTime) {
		if (info != null) {
			// Si es desconeix l'estat de l'aplicació, entenem que està DOWN
			SalutEntity salut = new SalutEntity();
			salut.setEntornAppId(entornAppId);
			salut.setData(currentMinuteTime);
			salut.setDataApp(toLocalDateTime(info.getData()));
			salut.setTipusRegistre(TipusRegistreSalut.MINUT);
			SalutEstat appEstat = toSalutEstat(info.getEstat().getEstat());
			salut.setAppEstat(appEstat);
			salut.updateAppCountByEstat(appEstat);
			SalutEstat bdEstat = toSalutEstat(info.getBd().getEstat());
			salut.setBdEstat(bdEstat);
			salut.updateBdCountByEstat(bdEstat);
			salut.setAppLatencia(info.getEstat().getLatencia());
			salut.setAppLatenciaMitjana(info.getEstat().getLatencia());
			salut.setBdLatencia(info.getBd().getLatencia());
			salut.setBdLatenciaMitjana(info.getBd().getLatencia());
			salut.setNumElements(1);
			SalutEntity saved = salutRepository.save(salut);
			crearSalutIntegracions(saved, info.getIntegracions());
			crearSalutSubsistemes(saved, info.getSubsistemes());
			crearSalutMissatges(saved, info.getMissatges());
			crearSalutDetalls(saved, info.getAltres());
			return saved.getId();
		}
		return null;
	}

	private void crearSalutIntegracions(
			SalutEntity salut,
			Collection<IntegracioSalut> integracions) {
		if (integracions != null) {
			integracions.forEach(i -> {
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
					SalutIntegracioEntity salutIntegracioFilla = new SalutIntegracioEntity();
					salutIntegracioFilla.setCodi(i.getCodi());
					salutIntegracioFilla.setEstat(toSalutEstat(i.getEstat()));
					salutIntegracioFilla.setLatencia(i.getLatencia());
					salutIntegracioFilla.setLatenciaMitjana(i.getLatencia());
					salutIntegracioFilla.setTotalOk(i.getPeticions() != null ? i.getPeticions().getTotalOk() : 0L);
					salutIntegracioFilla.setTotalError(i.getPeticions() != null ? i.getPeticions().getTotalError() : 0L);
					salutIntegracioFilla.setTotalTempsMig(i.getPeticions() != null && i.getPeticions().getTotalTempsMig() != null ? i.getPeticions().getTotalTempsMig() : 0);
					salutIntegracioFilla.setPeticionsOkUltimPeriode(i.getPeticions() != null && i.getPeticions().getPeticionsOkUltimPeriode() != null ? i.getPeticions().getPeticionsOkUltimPeriode() : 0L);
					salutIntegracioFilla.setPeticionsErrorUltimPeriode(i.getPeticions() != null && i.getPeticions().getPeticionsErrorUltimPeriode() != null ? i.getPeticions().getPeticionsErrorUltimPeriode() : 0L);
					salutIntegracioFilla.setTempsMigUltimPeriode(i.getPeticions() != null && i.getPeticions().getTempsMigUltimPeriode() != null ? i.getPeticions().getTempsMigUltimPeriode() : 0);
					salutIntegracioFilla.setEndpoint(i.getPeticions() != null ? i.getPeticions().getEndpoint() : null);
					salutIntegracioFilla.setSalut(salut);
					salutIntegracioFilla.setPare(salutIntegracioSaved);
				}
			});
		}
	}

	private void crearSalutSubsistemes(SalutEntity salut, List<SubsistemaSalut> subsistemes) {
		if (subsistemes != null) {
			subsistemes.forEach(s -> {
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
		if (missatges != null) {
			missatges.forEach(m -> {
				SalutMissatgeEntity salutMissatge = new SalutMissatgeEntity();
				salutMissatge.setData(toLocalDateTime(m.getData()));
				salutMissatge.setNivell(toSalutNivell(m.getNivell()));
				salutMissatge.setMissatge(m.getMissatge());
				salutMissatge.setSalut(salut);
				salutMissatgeRepository.save(salutMissatge);
			});
		}
	}

	private void crearSalutDetalls(SalutEntity salut, List<DetallSalut> missatges) {
		if (missatges != null) {
			missatges.forEach(d -> {
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

	private SalutNivell toSalutNivell(String nivell) {
		if ("error".equalsIgnoreCase(nivell)) {
			return SalutNivell.ERROR;
		} else if ("avis".equalsIgnoreCase(nivell) || "warn".equalsIgnoreCase(nivell)) {
			return SalutNivell.WARN;
		} else if ("info".equalsIgnoreCase(nivell)) {
			return SalutNivell.INFO;
		} else {
			return null;
		}
	}

	public LocalDateTime toLocalDateTime(Date dateToConvert) {
		return dateToConvert != null ? LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault()) : null;
	}


    // Compactat de dades de salut
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final int MINUTS_PER_AGRUPACIO = 4;
    public static Map<Long, CompactacioInfo> compactacioMap = new HashMap<>();

    @Data
    @Builder
    public static class CompactacioInfo {
        @Setter(AccessLevel.NONE)
        private int numeroMinutsAgrupats;

        public void incremenraMinutsAgrupats() {
            numeroMinutsAgrupats = ++numeroMinutsAgrupats % MINUTS_PER_AGRUPACIO;
        }
    }

    @Transactional
    public void buidatIcompactat(Long entornAppId, Long salutId, int numeroDiesAgrupacio) {

        Object lock = ENTORN_LOCKS.computeIfAbsent(entornAppId, k -> new Object());
        synchronized (lock) {
            try {
                log.info("Executant buidat i compactat de dades de salut. EntornAppId: {}, salutId: {}, numeroDiesAgrupacio: {}.",
                        entornAppId, salutId, numeroDiesAgrupacio);
                SalutEntity dadesSalut = salutRepository.findById(salutId).orElse(null);
                if (dadesSalut == null) {
                    log.warn("No s'ha trobat el registre de salut amb id {} per a l'entorn {}. Possiblement la transacció encara no està confirmada.", salutId, entornAppId);
                    return;
                }
                // Crear agregats per grups de minuts, hores i dies
                agregarGrupMinuts(entornAppId, dadesSalut, numeroDiesAgrupacio);
                agregarHora(entornAppId, dadesSalut);
                agregarDia(entornAppId, dadesSalut);

                // Eliminar registres de minuts més antics que 15 minuts
                LocalDateTime menys15m = dadesSalut.getData().minusMinutes(15);
                eliminarAntiguesSenseRollback(entornAppId, TipusRegistreSalut.MINUT, menys15m);

                // Eliminar registres de grups de minuts més antics que 1 hora
                LocalDateTime menys1h = dadesSalut.getData().minusHours(1).minusMinutes(3);
                eliminarAntiguesSenseRollback(entornAppId, TipusRegistreSalut.MINUTS, menys1h);

                // Eliminar registres de hores més antics que 1 dia
                LocalDateTime menys1d = dadesSalut.getData().minusDays(1).minusMinutes(59);
                eliminarAntiguesSenseRollback(entornAppId, TipusRegistreSalut.HORA, menys1d);

                // Eliminar rgistres de dies més antics que 1 mes
                LocalDateTime menys1M = dadesSalut.getData().minusMonths(1).minusHours(23).minusMinutes(59);
                eliminarAntiguesSenseRollback(entornAppId, TipusRegistreSalut.DIA, menys1M);
            } catch (Exception e) {
                log.error("Error durant el procés de buidat i compactat de salut", e);
                throw e;
            }
        }
    }

    private void eliminarAntiguesSenseRollback(Long entornAppId, TipusRegistreSalut tipus, LocalDateTime dataLlindar) {
        try {
            self.eliminarDadesSalutAntigues(entornAppId, tipus, dataLlindar);
        } catch (Exception ex) {
            log.warn("Error eliminant dades antigues ({}). Es continuarà sense rollback del buidat/compactat. entornAppId={}, data={} -> {}", tipus.name(), entornAppId, dataLlindar, ex.getMessage(), ex);
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void eliminarDadesSalutAntigues(Long entornAppId, TipusRegistreSalut tipus, LocalDateTime data) {
        log.debug("Eliminant dades de salut antigues. EntornAppId: {}, tipus: {}, data: {}", entornAppId, tipus, data);
//        List<SalutEntity> massaAntics = salutRepository.findByEntornAppIdAndTipusRegistreAndDataBefore(entornAppId, tipus, data);
        List<Long> idsAntics = salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(entornAppId, tipus, data);

        // Process ids in batches to avoid memory issues
        for (int i = 0; i < idsAntics.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, idsAntics.size());
            List<Long> batch = idsAntics.subList(i, end);
            // Eliminació del batch dins la mateixa transacció REQUIRES_NEW oberta a nivell superior
            eliminarLlista(batch);
        }
    }

    public void eliminarLlista(List<Long> salutIds) {
        if (salutIds == null || salutIds.isEmpty()) {
            log.debug("Cap registre de salut per eliminar (null o buit)");
            return;
        }
        int intents = 0;
        int maxIntents = 3;
        while (true) {
            try {
                log.info("Eliminant {} registres de salut antics...", salutIds.size());
                // Eliminar fills per assegurar integritat
                salutIntegracioRepository.deleteAllBySalutIdIn(salutIds);
                salutSubsistemaRepository.deleteAllBySalutIdIn(salutIds);
                salutMissatgeRepository.deleteAllBySalutIdIn(salutIds);
                salutDetallRepository.deleteAllBySalutIdIn(salutIds);
                // Eliminació en batch per reduir bloquejos
                salutRepository.deleteAllByIdInBatch(salutIds);
                log.info("Eliminat {} registres de salut antics", salutIds.size());
                return;
            } catch (RuntimeException ex) {
                intents++;
                if (isLockAcquisitionException(ex) && intents < maxIntents) {
                    long sleep = 100L + (long) (Math.random() * 200L);
                    log.info("Bloqueig en eliminar registres de salut (intent {}/{}). Es tornarà a intentar després de {}ms.", intents, maxIntents, sleep);
                    try { Thread.sleep(sleep); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    throw ex;
                }
            }
        }
    }

    private void agregarGrupMinuts(Long entornAppId, SalutEntity darreraActualitzacioSalut, int numeroDiesAgrupacio) {
        if (darreraActualitzacioSalut == null) return;

        if (numeroDiesAgrupacio == 1) {
            creaAgregatSalut(entornAppId, TipusRegistreSalut.MINUTS, darreraActualitzacioSalut);
        } else {
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

    /**
     * Crea un nou registre SalutEntity agregat a partir d'un únic registre de tipus MINUT.
     * No elimina el registre original.
     */
    public SalutEntity creaAgregatSalut(Long entornAppId, TipusRegistreSalut tipus, SalutEntity salut) {
        if (salut == null) return null;

        
        SalutEntity agregat = new SalutEntity();
        agregat.setEntornAppId(entornAppId);
        LocalDateTime data = salut.getData();
        if (tipus == TipusRegistreSalut.HORA) {
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
	                    integracio.addPeticionsOkUltimPeriode(si.getPeticionsOkUltimPeriode());
	                    integracio.addPeticionsErrorUltimPeriode(si.getPeticionsErrorUltimPeriode());
	                    integracio.addTempsMigUltimPeriode(si.getTempsMigUltimPeriode());
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
	                    subsistema.addPeticionsOkUltimPeriode(ss.getPeticionsOkUltimPeriode());
	                    subsistema.addPeticionsErrorUltimPeriode(ss.getPeticionsErrorUltimPeriode());
	                    subsistema.addTempsMigUltimPeriode(ss.getTempsMigUltimPeriode());
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

    private static int actualitzaInfoCompactacio(Long entornAppId) {
        CompactacioInfo compactacioInfo = compactacioMap.get(entornAppId);
        if (compactacioInfo == null) {
            compactacioInfo = CompactacioInfo.builder().numeroMinutsAgrupats(1).build();
            compactacioMap.put(entornAppId, compactacioInfo);
        } else {
            compactacioInfo.incremenraMinutsAgrupats();
        }
        return compactacioInfo.getNumeroMinutsAgrupats();
    }

    private boolean isFirstMinuteOfHour(LocalDateTime dateTime) {
        return dateTime != null && dateTime.getMinute() == 0;
    }

    private boolean isFirstMinuteOfDay(LocalDateTime dateTime) {
        return dateTime != null && dateTime.getHour() == 0 && dateTime.getMinute() == 0;
    }

    private boolean isLockAcquisitionException(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t instanceof CannotAcquireLockException || t instanceof LockAcquisitionException || t instanceof LockTimeoutException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

}


