package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventTypes;
import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigTipus;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.Salut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Comprovacions i creació d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaComprovacioHelper {

	private final AlarmaRepository alarmaRepository;
	private final SalutServiceClient salutServiceClient;
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
	private final AlarmaMailHelper alarmaMailHelper;
    private final ComandaSseEventPublisher comandaSseEventPublisher;

	public boolean comprovar(AlarmaConfigEntity alarmaConfig) {
		if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_CAIGUDA) {
			return comprovarAppCaiguda(alarmaConfig);
		} else if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_LATENCIA) {
			return comprovarAppLatencia(alarmaConfig);
		} else {
			log.error("Tipus d'alarma no suportat: {}", alarmaConfig.getTipus());
			return false;
		}
	}

	private boolean comprovarAppCaiguda(AlarmaConfigEntity alarmaConfig) {
		Salut salut = findSalutLast(alarmaConfig.getEntornAppId());
		boolean condicioAlarma = false;
		if (salut != null) {
			condicioAlarma = salut.getAppEstat().equals(EstatSalutEnum.DOWN.name()) || salut.getAppEstat().equals(EstatSalutEnum.ERROR.name());
		}
		if (condicioAlarma) {
			processarCondicioAfirmativa(alarmaConfig);
			return true;
		} else {
			processarCondicioNegativa(alarmaConfig);
			return false;
		}
	}

	private boolean comprovarAppLatencia(AlarmaConfigEntity alarmaConfig) {
		Salut salut = findSalutLast(alarmaConfig.getEntornAppId());
		boolean alarma = false;
		if (salut != null) {
			Integer latencia = salut.getAppLatencia();
			if (latencia != null && alarmaConfig.getCondicio() != null) {
				switch (alarmaConfig.getCondicio()) {
					case MAJOR:
						alarma = latencia > alarmaConfig.getValor().intValue();
						break;
					case MAJOR_IGUAL:
						alarma = latencia >= alarmaConfig.getValor().intValue();
						break;
					case MENOR:
						alarma = latencia < alarmaConfig.getValor().intValue();
						break;
					case MENOR_IGUAL:
						alarma = latencia <= alarmaConfig.getValor().intValue();
						break;
				}
			 }
		}
		if (alarma) {
			processarCondicioAfirmativa(alarmaConfig);
			return true;
		} else {
			processarCondicioNegativa(alarmaConfig);
			return false;
		}
	}

	/**
	 * Processa la condició afirmativa d'una alarma basada en la seva configuració.
	 * Aquesta lògica s'encarrega de gestionar la creació, activació i enviament
	 * de correus per a alarmes segons el seu estat i condicions definides.
	 *
	 * @param alarmaConfig Entitat de configuració de l'alarma
	 */
	private void processarCondicioAfirmativa(AlarmaConfigEntity alarmaConfig) {
		Optional<AlarmaEntity> optionalAlarmaAnteriorNoFinalitzada = alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(alarmaConfig);
		AlarmaEntity alarmaActivada = null;

		if (hasAlarmaConfigPeriodes(alarmaConfig)) {
			if (optionalAlarmaAnteriorNoFinalitzada.isEmpty()) {
				Alarma alarma = new Alarma();
				alarma.setEntornAppId(alarmaConfig.getEntornAppId());
				alarma.setEstat(AlarmaEstat.ESBORRANY);
				alarma.setMissatge(alarmaConfig.getMissatge());
				alarmaRepository.save(
						AlarmaEntity.builder().
								alarma(alarma).
								alarmaConfig(alarmaConfig).
								build());
				log.debug("Nova alarma de tipus esborrany creada (configId={}, configNom={}, destinatari={}",
						alarmaConfig.getId(),
						alarmaConfig.getNom(),
						alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
				return;
			}
			AlarmaEntity alarmaAnteriorNoFinalitzada = optionalAlarmaAnteriorNoFinalitzada.get();

			if (alarmaAnteriorNoFinalitzada.getEstat() == AlarmaEstat.ESBORRANY) {
				Duration duration = Duration.between(alarmaAnteriorNoFinalitzada.getCreatedDate(), LocalDateTime.now());
				boolean activar = false;
				switch (alarmaConfig.getPeriodeUnitat()) {
					case SEGONS:
						activar = duration.getSeconds() > alarmaConfig.getPeriodeValor().intValue();
						break;
					case MINUTS:
						activar = duration.getSeconds() / 60 > alarmaConfig.getPeriodeValor().intValue();
						break;
					case HORES:
						activar = duration.getSeconds() / 3600 > alarmaConfig.getPeriodeValor().intValue();
						break;
					case DIES:
						activar = duration.getSeconds() / 3600 * 24 > alarmaConfig.getPeriodeValor().intValue();
						break;
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
			enviarCorreuAlarma(alarmaActivada);
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
		if (optionalAlarmaAnteriorNoFinalitzada.isEmpty()) return;
		AlarmaEntity alarmaAnteriorNoFinalitzada = optionalAlarmaAnteriorNoFinalitzada.get();

		if (alarmaAnteriorNoFinalitzada.getEstat() == AlarmaEstat.ESBORRANY) {
			alarmaRepository.delete(alarmaAnteriorNoFinalitzada);
		} else {
			alarmaAnteriorNoFinalitzada.setDataFinalitzacio(LocalDateTime.now());
            publishActiveAlarmsChangedEvent();
		}
	}

    private void publishActiveAlarmsChangedEvent() {
        comandaSseEventPublisher.publish(ComandaSseEventTypes.ACTIVE_ALARMS_CHANGED);
    }

	private boolean hasAlarmaConfigPeriodes(AlarmaConfigEntity alarmaConfig) {
		return alarmaConfig.getPeriodeValor() != null && alarmaConfig.getPeriodeUnitat() != null;
	}

	private void enviarCorreuAlarma(AlarmaEntity alarma) {
        alarmaMailHelper.sendAlarmaUser(alarma);

        if (alarma.getAlarmaConfig().isCorreuGeneric()) {
			alarmaMailHelper.sendAlarmaGeneric(alarma);
		}
	}

	private Salut findSalutLast(Long entornAppId) {
		PagedModel<EntityModel<Salut>> saluts = salutServiceClient.find(
				null,
				"entornAppId:" + entornAppId,
				null,
				null,
				"0",
				1,
				new String[] { "id,desc" },
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		if (saluts == null) return null;
		return saluts.getContent().stream().
				findFirst().
				map(EntityModel::getContent).
				orElse(null);
	}

}
