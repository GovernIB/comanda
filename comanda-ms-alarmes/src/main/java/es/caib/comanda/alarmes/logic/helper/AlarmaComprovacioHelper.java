package es.caib.comanda.alarmes.logic.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.alarmes.logic.event.AlarmaMailEventPublisher;
import es.caib.comanda.alarmes.logic.event.AlarmaMailEventType;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigCondicio;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigPeriodeUnitat;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigRegla;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaAmbit;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaComparador;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaMetrica;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaOperador;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigReglaTipusNode;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigTipus;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventTypes;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.Salut;
import es.caib.comanda.client.model.SalutDetall;
import es.caib.comanda.client.model.SalutIntegracio;
import es.caib.comanda.client.model.SalutSubsistema;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprovacions i creació d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaComprovacioHelper {

	private static final long DEFAULT_SALUT_FRESHNESS_SECONDS = 120;
	private static final long DEFAULT_RECOVERY_STABILITY_SECONDS = 180;
	private static final String[] SALUT_PERSPECTIVES = {"SAL_INTEGRACIONS", "SAL_SUBSISTEMES", "SAL_DETALLS"};
	private static final Pattern NUMERIC_WITH_UNIT_PATTERN = Pattern.compile("([0-9.,]+)\\s*([A-Za-z%]*)");

	private final AlarmaRepository alarmaRepository;
	private final SalutServiceClient salutServiceClient;
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
	private final AlarmaMailEventPublisher alarmaMailEventPublisher;
    private final ComandaSseEventPublisher comandaSseEventPublisher;
	private final ParametresHelper parametresHelper;
	private final ObjectMapper objectMapper;
	private final Map<Long, LocalDateTime> recoveryStableSinceByConfigId = new ConcurrentHashMap<>();

	public boolean comprovar(AlarmaConfigEntity alarmaConfig) {
		Salut salut = findSalutLast(alarmaConfig.getEntornAppId());
		if (!isFreshSalut(salut)) {
			processarCondicioIndeterminada(alarmaConfig, salut);
			return false;
		}

		boolean condicioAlarma = evaluateAlarmCondition(alarmaConfig, salut);
		if (condicioAlarma) {
			processarCondicioAfirmativa(alarmaConfig);
			return true;
		}

		processarCondicioNegativa(alarmaConfig);
		return false;
	}

	private boolean evaluateAlarmCondition(AlarmaConfigEntity alarmaConfig, Salut salut) {
		AlarmaConfigRegla regla = readRule(alarmaConfig);
		if (regla != null) {
			return evaluateRule(regla, salut);
		}
		return evaluateLegacyCondition(alarmaConfig, salut);
	}

	private boolean evaluateRule(AlarmaConfigRegla regla, Salut salut) {
		if (regla.getTipusNode() == AlarmaConfigReglaTipusNode.GRUP) {
			List<AlarmaConfigRegla> fills = regla.getFills();
			if (fills == null || fills.isEmpty()) {
				return false;
			}
			if (regla.getOperador() == AlarmaConfigReglaOperador.OR) {
				return fills.stream().filter(Objects::nonNull).anyMatch(fill -> evaluateRule(fill, salut));
			}
			return fills.stream().filter(Objects::nonNull).allMatch(fill -> evaluateRule(fill, salut));
		}
		return evaluatePredicate(regla, salut);
	}

	private boolean evaluatePredicate(AlarmaConfigRegla regla, Salut salut) {
		if (regla.getAmbit() == null || regla.getMetrica() == null || regla.getComparador() == null) {
			return false;
		}

		if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESTAT) {
			return compareText(resolveStateValue(regla, salut), regla.getComparador(), regla.getValorsText());
		}
		return compareNumeric(resolveNumericMetric(regla, salut), regla.getComparador(), regla.getValorNumeric());
	}

	private String resolveStateValue(AlarmaConfigRegla regla, Salut salut) {
		if (regla.getAmbit() == AlarmaConfigReglaAmbit.APLICACIO) {
			return salut.getAppEstat();
		}
		if (regla.getAmbit() == AlarmaConfigReglaAmbit.SUBSISTEMA) {
			return salut.getSubsistemes() == null ? null : salut.getSubsistemes().stream()
			                                               .filter(item -> item != null && equalsCode(item.getCodi(), regla.getCodiObjecte()))
			                                               .findFirst()
			                                               .map(SalutSubsistema::getEstat)
			                                               .map(Enum::name)
			                                               .orElse(null);
		}
		if (regla.getAmbit() == AlarmaConfigReglaAmbit.INTEGRACIO) {
			return salut.getIntegracions() == null ? null : salut.getIntegracions().stream()
			                                                .filter(item -> item != null && equalsCode(item.getCodi(), regla.getCodiObjecte()))
			                                                .findFirst()
			                                                .map(SalutIntegracio::getEstat)
			                                                .map(Enum::name)
			                                                .orElse(null);
		}
		return null;
	}

	private BigDecimal resolveNumericMetric(AlarmaConfigRegla regla, Salut salut) {
		if (regla.getMetrica() == AlarmaConfigReglaMetrica.LATENCIA) {
			return salut.getAppLatencia() == null ? null : BigDecimal.valueOf(salut.getAppLatencia());
		}
		if (regla.getMetrica() == AlarmaConfigReglaMetrica.CARREGA_SISTEMA) {
			return parsePlainNumber(findDetallValor(salut, "SCPU"));
		}
		if (regla.getMetrica() == AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE) {
			return parseStorageInMegabytes(findDetallValor(salut, "MED"));
		}
		if (regla.getMetrica() == AlarmaConfigReglaMetrica.ESPAI_DISC_LLIURE) {
			return parseStorageInMegabytes(findDetallValor(salut, "EDL"));
		}
		return null;
	}

	private boolean compareText(String actualValue, AlarmaConfigReglaComparador comparador, List<String> expectedValues) {
		if (actualValue == null || expectedValues == null || expectedValues.isEmpty()) {
			return false;
		}
		String normalizedActual = actualValue.trim().toUpperCase(Locale.ROOT);
		List<String> normalizedExpected = expectedValues.stream()
				.filter(Objects::nonNull)
				.map(value -> value.trim().toUpperCase(Locale.ROOT))
				.collect(Collectors.toList());
		switch (comparador) {
			case EN:
				return normalizedExpected.contains(normalizedActual);
			case IGUAL:
				return normalizedExpected.stream().findFirst().map(normalizedActual::equals).orElse(false);
			case DIFERENT:
				return normalizedExpected.stream().findFirst().map(value -> !normalizedActual.equals(value)).orElse(false);
			default:
				return false;
		}
	}

	private boolean compareNumeric(BigDecimal actualValue, AlarmaConfigReglaComparador comparador, BigDecimal expectedValue) {
		if (actualValue == null || expectedValue == null) {
			return false;
		}
		int comparison = actualValue.compareTo(expectedValue);
		switch (comparador) {
			case MAJOR:
				return comparison > 0;
			case MAJOR_IGUAL:
				return comparison >= 0;
			case MENOR:
				return comparison < 0;
			case MENOR_IGUAL:
				return comparison <= 0;
			case IGUAL:
				return comparison == 0;
			case DIFERENT:
				return comparison != 0;
			default:
				return false;
		}
	}

	private boolean evaluateLegacyCondition(AlarmaConfigEntity alarmaConfig, Salut salut) {
		if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_CAIGUDA) {
			return "DOWN".equalsIgnoreCase(salut.getAppEstat());
		}
		if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_LATENCIA) {
			Integer latencia = salut.getAppLatencia();
			if (latencia == null || alarmaConfig.getCondicio() == null || alarmaConfig.getValor() == null) {
				return false;
			}
			switch (alarmaConfig.getCondicio()) {
				case MAJOR:
					return latencia > alarmaConfig.getValor().intValue();
				case MAJOR_IGUAL:
					return latencia >= alarmaConfig.getValor().intValue();
				case MENOR:
					return latencia < alarmaConfig.getValor().intValue();
				case MENOR_IGUAL:
					return latencia <= alarmaConfig.getValor().intValue();
				default:
					return false;
			}
		}
		log.error("Tipus d'alarma no suportat: {}", alarmaConfig.getTipus());
		return false;
	}

	/**
	 * Processa la condició afirmativa d'una alarma basada en la seva configuració.
	 * Aquesta lògica s'encarrega de gestionar la creació, activació i enviament
	 * de correus per a alarmes segons el seu estat i condicions definides.
	 *
	 * @param alarmaConfig Entitat de configuració de l'alarma
	 */
	private void processarCondicioAfirmativa(AlarmaConfigEntity alarmaConfig) {
		clearRecoveryTracking(alarmaConfig);
		Optional<AlarmaEntity> optionalAlarmaAnteriorNoFinalitzada = alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(alarmaConfig);
		AlarmaEntity alarmaActivada = null;

		if (hasAlarmaConfigPeriodes(alarmaConfig)) {
			if (optionalAlarmaAnteriorNoFinalitzada.isEmpty()) {
				Alarma alarma = new Alarma();
				alarma.setEntornAppId(alarmaConfig.getEntornAppId());
				alarma.setEstat(AlarmaEstat.ESBORRANY);
				alarma.setMissatge(alarmaConfig.getMissatge());
				alarmaRepository.save(AlarmaEntity.builder().alarma(alarma).alarmaConfig(alarmaConfig).build());
				log.debug("Nova alarma de tipus esborrany creada (configId={}, configNom={}, destinatari={})",
						alarmaConfig.getId(),
						alarmaConfig.getNom(),
						alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
				return;
			}
			AlarmaEntity alarmaAnteriorNoFinalitzada = optionalAlarmaAnteriorNoFinalitzada.get();

			if (alarmaAnteriorNoFinalitzada.getEstat() == AlarmaEstat.ESBORRANY) {
				Duration duration = Duration.between(alarmaAnteriorNoFinalitzada.getCreatedDate(), LocalDateTime.now());
				boolean activar = false;
				if (alarmaConfig.getPeriodeUnitat() == AlarmaConfigPeriodeUnitat.SEGONS) {
					activar = duration.getSeconds() > alarmaConfig.getPeriodeValor().intValue();
				} else if (alarmaConfig.getPeriodeUnitat() == AlarmaConfigPeriodeUnitat.MINUTS) {
					activar = duration.getSeconds() / 60 > alarmaConfig.getPeriodeValor().intValue();
				} else if (alarmaConfig.getPeriodeUnitat() == AlarmaConfigPeriodeUnitat.HORES) {
					activar = duration.getSeconds() / 3600 > alarmaConfig.getPeriodeValor().intValue();
				} else if (alarmaConfig.getPeriodeUnitat() == AlarmaConfigPeriodeUnitat.DIES) {
					activar = duration.getSeconds() / 3600 * 24 > alarmaConfig.getPeriodeValor().intValue();
				}
				if (activar) {
					alarmaAnteriorNoFinalitzada.setEstat(AlarmaEstat.ACTIVA);
					alarmaAnteriorNoFinalitzada.setDataActivacio(LocalDateTime.now());
					alarmaActivada = alarmaAnteriorNoFinalitzada;
                    publishActiveAlarmsChangedEvent();
					log.debug("Alarma de tipus esborrany activada (configId={}, configNom={}, destinatari={}",
							alarmaConfig.getId(),
							alarmaConfig.getNom(),
							alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
				}
			}
		} else {
			if (optionalAlarmaAnteriorNoFinalitzada.isPresent()) {
				// Si una alarma per període es canvia a alarma sense període, les alarmes en esborrany que s'han quedat
				// pendents d'activar-se poden activar-se sempre que la condició segueixi activa.
				if (optionalAlarmaAnteriorNoFinalitzada.get().getEstat() == AlarmaEstat.ESBORRANY) {
					optionalAlarmaAnteriorNoFinalitzada.get().setEstat(AlarmaEstat.ACTIVA);
					optionalAlarmaAnteriorNoFinalitzada.get().setDataActivacio(LocalDateTime.now());
					alarmaActivada = optionalAlarmaAnteriorNoFinalitzada.get();
                    publishActiveAlarmsChangedEvent();
				}
				return;
			}

			Alarma alarma = new Alarma();
			alarma.setEntornAppId(alarmaConfig.getEntornAppId());
			alarma.setEstat(AlarmaEstat.ACTIVA);
			alarma.setMissatge(alarmaConfig.getMissatge());
			alarma.setDataActivacio(LocalDateTime.now());
			alarmaActivada = alarmaRepository.save(
					AlarmaEntity.builder().
							alarma(alarma).
							alarmaConfig(alarmaConfig).
							build());
            publishActiveAlarmsChangedEvent();
			log.debug("Nova alarma activa creada (configId={}, configNom={}, destinatari={}",
					alarmaConfig.getId(),
					alarmaConfig.getNom(),
					alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
		}

		if (alarmaActivada != null) {
			publishAlarmaMailEvent(alarmaActivada, AlarmaMailEventType.ACTIVACIO);
		}
	}

	/**
	 * Processa la condició negativa d'una alarma basada en la seva configuració.
	 * Aquesta lògica s'encarrega de cercar una alarma no finalitzada associada
	 * a la configuració, i en funció del seu estat, eliminar-la o marcar-la com
	 * finalitzada.
	 *
	 * @param alarmaConfig Entitat de configuració de l'alarma. Conté la informació
	 * necessària per identificar i operar sobre les alarmes associades.
	 */
	private void processarCondicioNegativa(AlarmaConfigEntity alarmaConfig) {
		Optional<AlarmaEntity> optionalAlarmaAnteriorNoFinalitzada = alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(alarmaConfig);
		if (optionalAlarmaAnteriorNoFinalitzada.isEmpty()) {
			clearRecoveryTracking(alarmaConfig);
			return;
		}
		AlarmaEntity alarmaAnteriorNoFinalitzada = optionalAlarmaAnteriorNoFinalitzada.get();

		if (alarmaAnteriorNoFinalitzada.getEstat() == AlarmaEstat.ESBORRANY) {
			clearRecoveryTracking(alarmaConfig);
			alarmaRepository.delete(alarmaAnteriorNoFinalitzada);
		} else {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime stableSince = recoveryStableSinceByConfigId.computeIfAbsent(alarmaConfig.getId(), ignored -> now);
			long recoveryStabilitySeconds = getRecoveryStabilitySeconds();
			if (Duration.between(stableSince, now).getSeconds() < recoveryStabilitySeconds) {
				log.debug("Recuperació detectada però encara no estable (configId={}, estableDesDe={}, estabilitzacioSegons={})",
						alarmaConfig.getId(),
						stableSince,
						recoveryStabilitySeconds);
				return;
			}
			alarmaAnteriorNoFinalitzada.setDataFinalitzacio(now);
			clearRecoveryTracking(alarmaConfig);
            publishActiveAlarmsChangedEvent();
            if (alarmaConfig.isNotificacioFinalitzada()) {
                publishAlarmaMailEvent(alarmaAnteriorNoFinalitzada, AlarmaMailEventType.RECUPERACIO);
            }
		}
	}

	private void processarCondicioIndeterminada(AlarmaConfigEntity alarmaConfig, Salut salut) {
		clearRecoveryTracking(alarmaConfig);
		if (salut == null) {
			log.debug("No es modifica l'alarma perquè no hi ha dades de salut recents per l'entornApp {}", alarmaConfig.getEntornAppId());
			return;
		}
		log.debug("No es modifica l'alarma perquè la darrera salut no és prou recent (configId={}, entornAppId={}, salutData={}, frescorMaxSegons={})",
				alarmaConfig.getId(),
				alarmaConfig.getEntornAppId(),
				salut.getData(),
				getSalutFreshnessSeconds());
	}

    private void publishActiveAlarmsChangedEvent() {
        comandaSseEventPublisher.publish(ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED);
    }

	private boolean hasAlarmaConfigPeriodes(AlarmaConfigEntity alarmaConfig) {
		return alarmaConfig.getPeriodeValor() != null && alarmaConfig.getPeriodeUnitat() != null;
	}

    private void publishAlarmaMailEvent(AlarmaEntity alarma, AlarmaMailEventType tipusEvent) {
        alarmaMailEventPublisher.publish(alarma, tipusEvent);
    }

	private boolean isFreshSalut(Salut salut) {
		if (salut == null || salut.getData() == null) {
			return false;
		}
		long salutFreshnessSeconds = getSalutFreshnessSeconds();
		long ageSeconds = Math.abs(Duration.between(salut.getData(), LocalDateTime.now()).getSeconds());
		return ageSeconds <= salutFreshnessSeconds;
	}

	private long getSalutFreshnessSeconds() {
		return parametresHelper.getParametreEnter(
				BaseConfig.PROP_ALARMA_SALUT_FRESHNESS_SECONDS,
				(int) DEFAULT_SALUT_FRESHNESS_SECONDS);
	}

	private long getRecoveryStabilitySeconds() {
		return parametresHelper.getParametreEnter(
				BaseConfig.PROP_ALARMA_RECOVERY_STABILITY_SECONDS,
				(int) DEFAULT_RECOVERY_STABILITY_SECONDS);
	}

	private void clearRecoveryTracking(AlarmaConfigEntity alarmaConfig) {
		if (alarmaConfig != null && alarmaConfig.getId() != null) {
			recoveryStableSinceByConfigId.remove(alarmaConfig.getId());
		}
	}

	private Salut findSalutLast(Long entornAppId) {
		PagedModel<EntityModel<Salut>> saluts = salutServiceClient.find(
				null,
				"entornAppId:" + entornAppId,
				null,
				SALUT_PERSPECTIVES,
				"0",
				1,
				new String[]{"id,desc"},
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		if (saluts == null) {
			return null;
		}
		return saluts.getContent().stream().
				findFirst().
				map(EntityModel::getContent).
				orElse(null);
	}

	private AlarmaConfigRegla readRule(AlarmaConfigEntity alarmaConfig) {
		if (alarmaConfig.getRuleJson() != null && !alarmaConfig.getRuleJson().isBlank()) {
			try {
				return objectMapper.readValue(alarmaConfig.getRuleJson(), AlarmaConfigRegla.class);
			} catch (JsonProcessingException ex) {
				log.error("No s'ha pogut llegir la regla de l'alarma {}", alarmaConfig.getId(), ex);
			}
		}
		return legacyRule(alarmaConfig);
	}

	private AlarmaConfigRegla legacyRule(AlarmaConfigEntity alarmaConfig) {
		if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_CAIGUDA) {
			return AlarmaConfigRegla.builder()
					.tipusNode(AlarmaConfigReglaTipusNode.GRUP)
					.operador(AlarmaConfigReglaOperador.AND)
					.fills(List.of(
							AlarmaConfigRegla.builder()
									.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
									.ambit(AlarmaConfigReglaAmbit.APLICACIO)
									.metrica(AlarmaConfigReglaMetrica.ESTAT)
									.comparador(AlarmaConfigReglaComparador.EN)
									.valorsText(List.of("DOWN"))
									.build()))
					.build();
		}
		if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_LATENCIA) {
			return AlarmaConfigRegla.builder()
					.tipusNode(AlarmaConfigReglaTipusNode.GRUP)
					.operador(AlarmaConfigReglaOperador.AND)
					.fills(List.of(
							AlarmaConfigRegla.builder()
									.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
									.ambit(AlarmaConfigReglaAmbit.SISTEMA)
									.metrica(AlarmaConfigReglaMetrica.LATENCIA)
									.comparador(mapLegacyComparador(alarmaConfig.getCondicio()))
									.valorNumeric(alarmaConfig.getValor())
									.build()))
					.build();
		}
		return null;
	}

	private AlarmaConfigReglaComparador mapLegacyComparador(AlarmaConfigCondicio condicio) {
		if (condicio == null) {
			return AlarmaConfigReglaComparador.MAJOR;
		}
		switch (condicio) {
			case MAJOR:
				return AlarmaConfigReglaComparador.MAJOR;
			case MAJOR_IGUAL:
				return AlarmaConfigReglaComparador.MAJOR_IGUAL;
			case MENOR:
				return AlarmaConfigReglaComparador.MENOR;
			case MENOR_IGUAL:
				return AlarmaConfigReglaComparador.MENOR_IGUAL;
			default:
				return AlarmaConfigReglaComparador.MAJOR;
		}
	}

	private String findDetallValor(Salut salut, String codi) {
		if (salut.getDetalls() == null) {
			return null;
		}
		return salut.getDetalls().stream()
				.filter(item -> item != null && equalsCode(item.getCodi(), codi))
				.findFirst()
				.map(SalutDetall::getValor)
				.orElse(null);
	}

	private boolean equalsCode(String left, String right) {
		return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
	}

	private BigDecimal parsePlainNumber(String raw) {
		ParsedMeasure parsedMeasure = parseMeasure(raw);
		return parsedMeasure == null ? null : parsedMeasure.value();
	}

	private BigDecimal parseStorageInMegabytes(String raw) {
		ParsedMeasure parsedMeasure = parseMeasure(raw);
		if (parsedMeasure == null) {
			return null;
		}
		String unit = parsedMeasure.unit();
		BigDecimal value = parsedMeasure.value();
		if (unit == null || unit.isBlank() || "MB".equals(unit)) {
			return value;
		}
		if ("GB".equals(unit)) {
			return value.multiply(BigDecimal.valueOf(1024));
		}
		if ("TB".equals(unit)) {
			return value.multiply(BigDecimal.valueOf(1024 * 1024L));
		}
		if ("KB".equals(unit)) {
			return value.divide(BigDecimal.valueOf(1024), BigDecimal.ROUND_HALF_UP);
		}
		if ("B".equals(unit)) {
			return value.divide(BigDecimal.valueOf(1024 * 1024L), BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	private ParsedMeasure parseMeasure(String raw) {
		if (raw == null) {
			return null;
		}
		String normalized = raw.trim();
		if (normalized.isBlank() || "No disponible".equalsIgnoreCase(normalized)) {
			return null;
		}
		Matcher matcher = NUMERIC_WITH_UNIT_PATTERN.matcher(normalized.replace('\u00A0', ' '));
		if (!matcher.find()) {
			return null;
		}
		String numberPart = normalizeNumber(matcher.group(1));
		if (numberPart == null) {
			return null;
		}
		try {
			return new ParsedMeasure(new BigDecimal(numberPart), matcher.group(2).toUpperCase(Locale.ROOT));
		} catch (NumberFormatException ex) {
			log.debug("No s'ha pogut parsejar el valor numeric '{}'", raw, ex);
			return null;
		}
	}

	private String normalizeNumber(String raw) {
		if (raw == null) {
			return null;
		}
		String normalized = raw.trim();
		if (normalized.contains(",") && normalized.contains(".")) {
			if (normalized.lastIndexOf(',') > normalized.lastIndexOf('.')) {
				normalized = normalized.replace(".", "").replace(',', '.');
			} else {
				normalized = normalized.replace(",", "");
			}
		} else if (normalized.contains(",")) {
			normalized = normalized.replace(',', '.');
		}
		return normalized;
	}

	private static class ParsedMeasure {
		private final BigDecimal value;
		private final String unit;

		private ParsedMeasure(BigDecimal value, String unit) {
			this.value = value;
			this.unit = unit;
		}

		public BigDecimal value() {
			return value;
		}

		public String unit() {
			return unit;
		}
	}
}
