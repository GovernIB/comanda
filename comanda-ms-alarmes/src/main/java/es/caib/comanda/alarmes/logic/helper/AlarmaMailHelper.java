package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.logic.event.AlarmaMailEventType;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enviament de correus d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaMailHelper {
	private static final DateTimeFormatter ALARMA_DIA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a les' HH:mm");

	@Value("${" + BaseConfig.PROP_HTTPAUTH_USERNAME + ":#{null}}")
	private String httpAuthUsername;
	@Value("${" + BaseConfig.PROP_STATS_AUTH_USER + ":#{null}}")
	private String statsAuthUsername;
	private final AlarmaClientHelper alarmaClientHelper;
	private final MailHelper mailHelper;
	private final UserInformationHelper userInformationHelper;
	private final AlarmaRepository alarmaRepository;
	private final ParametresHelper parametresHelper;


	private String generateIndividualAlarmaSubject(AlarmaEntity alarma, AlarmaMailEventType tipusEvent) {
		String prefix = tipusEvent == AlarmaMailEventType.RECUPERACIO ? "[COMANDA] Alarma finalitzada" : "[COMANDA] Alarma activada";
		return prefix + (Strings.isNotBlank(alarma.getAlarmaConfig().getNom()) ? ": " + alarma.getAlarmaConfig().getNom() : "");
	}

	private String generateAlarmaBodyMessage(AlarmaEntity alarma) {
		String formattedNom = Strings.isNotBlank(alarma.getAlarmaConfig().getNom()) ? "\"" + alarma.getAlarmaConfig().getNom() + "\" " : "";
		EntornApp alarmaEntornApp = alarmaClientHelper.entornAppFindById(alarma.getEntornAppId());
		App alarmaApp = alarmaClientHelper.appFindById(alarmaEntornApp.getApp().getId());
		Entorn alarmaEntorn = alarmaClientHelper.entornById(alarmaEntornApp.getEntorn().getId());
		String app = alarmaApp.getNom();
		String entorn = alarmaEntorn.getNom();
		String message = alarma.getMissatge();
		String dataActivacio = alarma.getDataActivacio().format(ALARMA_DIA_FORMATTER);
		String missatgeFinalitzacio = alarma.getDataFinalitzacio() != null ? "\nFinalitzada el " + alarma.getDataFinalitzacio().format(ALARMA_DIA_FORMATTER) : "";

		return "Alarma " + formattedNom + "activada el " + dataActivacio +
				" per a l'aplicació " + app + " - " + entorn + ":\n" +
				message + missatgeFinalitzacio;
	}

	public void sendAlarmaGeneric(AlarmaEntity alarma, AlarmaMailEventType tipusEvent) {
        EntornApp alarmaEntornApp = alarmaClientHelper.entornAppFindById(alarma.getEntornAppId());
		if (alarmaEntornApp == null || Strings.isEmpty(alarmaEntornApp.getAlarmesEmail())) {
			log.debug("[EML] No s'envia correu genèric (entornAppId={}, alarmaEntornApp={}, alarmesEmail={})",
					alarma.getEntornAppId(), alarmaEntornApp != null ? alarmaEntornApp.getId() : "null",
					alarmaEntornApp != null ? alarmaEntornApp.getAlarmesEmail() : "null");
			if (isLogActivacio()) {
				log.info("[EML] Correu genèric omès: entornApp sense email configurat (entornAppId={})",
						alarma.getEntornAppId());
			}
			return;
		}
		if (isLogActivacio()) {
			log.info("[EML] Enviant correu genèric a {} (entornAppId={}, tipusEvent={})",
					alarmaEntornApp.getAlarmesEmail(), alarma.getEntornAppId(), tipusEvent);
		}

		MonitorAlarmes monitor = new MonitorAlarmes(
				alarma.getEntornAppId(),
				MonitorAlarmes.ENVIAMENT_CORREU_GENERIC,
				alarmaEntornApp.getAlarmesEmail(),
				alarma.getAlarmaConfig().getCreatedBy(),
				alarmaClientHelper);
		monitor.startAction();

		try {
			boolean sent = sendAlarmaMail(
					alarmaEntornApp.getAlarmesEmail(),
					"Correu genèric (" + alarmaEntornApp.getApp().getNom() + " - " + alarmaEntornApp.getEntorn().getNom() + ")",
					generateIndividualAlarmaSubject(alarma, tipusEvent),
					generateAlarmaBodyMessage(alarma)
			);
			if (sent) {
				monitor.endAction();
			} else {
				monitor.endAction(new IllegalStateException("No s'ha pogut enviar el correu generic d'alarma"),
						"No s'ha pogut enviar el correu generic d'alarma");
			}
		} catch (Exception ex) {
			monitor.endAction(ex, "Error enviant correu generic d'alarma");
			log.error("Error enviant correu d'alarma genèrica", ex);
		}
	}

	public void sendAlarmaUser(AlarmaEntity alarma, AlarmaMailEventType tipusEvent) {
		if (alarma.getAlarmaConfig().isAdmin()) {
			log.debug("[EML] Processant correu d'alarma admin (configId={}, tipusEvent={})",
					alarma.getAlarmaConfig().getId(), tipusEvent);
			String[] adminUsers;
			try {
				adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
			} catch (UserInformationHelper.UserInformationException e) {
				String createdBy = alarma.getAlarmaConfig().getCreatedBy();
				log.warn("[EML] Error obtenint usuaris administradors per LDAP; s'envia el correu al creador de l'alarma ({}) (configId={}): {}",
						createdBy, alarma.getAlarmaConfig().getId(), e.getMessage());
				if (createdBy != null) {
					sendAlarmaMailToUserWithProfileCheck(alarma, createdBy, tipusEvent);
				}
				return;
			}
			if (isLogActivacio()) {
				log.info("[EML] Alarma admin configId={} -> {} usuaris administradors trobats",
						alarma.getAlarmaConfig().getId(), adminUsers.length);
			}
			Arrays.stream(adminUsers).forEach(adminUser ->
					sendAlarmaMailToUserWithProfileCheck(alarma, adminUser, tipusEvent));
		} else {
			String username = alarma.getAlarmaConfig().getCreatedBy();
			if (isLogActivacio()) {
				log.info("[EML] Processant correu d'alarma usuari (configId={}, username={}, tipusEvent={})",
						alarma.getAlarmaConfig().getId(), username, tipusEvent);
			}
			sendAlarmaMailToUserWithProfileCheck(alarma, username, tipusEvent);
		}
	}

	private void sendAlarmaMailToUserWithProfileCheck(AlarmaEntity alarma, String username, AlarmaMailEventType tipusEvent) {
		Usuari user = userInformationHelper.usuariFindByUsername(username);
		if (user == null) {
			log.warn("[EML] No s'ha enviat el correu a {} perquè l'usuari no s'ha trobat (configId={})",
					username, alarma.getAlarmaConfig().getId());
			return;
		}
		if (!isUserProfileAlarmaActiva(user)) {
			log.warn("[EML] No s'ha enviat el correu a {} perquè alarmaMail=false o usuari exclòs de notificacions (configId={})",
					username, alarma.getAlarmaConfig().getId());
			return;
		}
		if (user.isAlarmaMailAgrupar()) {
			log.debug("[EML] No s'envia correu individual a {} perquè té configurat enviament agrupat (configId={})",
					username, alarma.getAlarmaConfig().getId());
			if (isLogActivacio()) {
				log.info("[EML] Omès correu individual a {} (prefereix agrupat, configId={})",
						username, alarma.getAlarmaConfig().getId());
			}
			return;
		}
		if (isLogActivacio()) {
			log.info("[EML] Enviant correu a {} (configId={}, tipusEvent={})",
					username, alarma.getAlarmaConfig().getId(), tipusEvent);
		}
		sendAlarmaMailForUser(alarma, username, tipusEvent);
	}

	public long sendAlarmesAgrupades() {
		LocalDateTime dataDesde = LocalDateTime.now().minusHours(24);

		// Envia les alarmes dels administradors (activades + finalitzades)
		List<AlarmaEntity> alarmesPendentsAdmin = alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(
				dataDesde);
		List<AlarmaEntity> alarmesFinalitzadesAdmin = alarmaRepository.findByAlarmaConfigAdminTrueAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(
				dataDesde);
		List<AlarmaEntity> totesAlarmesAdmin = mergeAlarmes(alarmesPendentsAdmin, alarmesFinalitzadesAdmin);
        long adminMailCount = 0;
        if (!totesAlarmesAdmin.isEmpty()) {
            try {
                String[] adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
                adminMailCount = Arrays.stream(adminUsers).filter(adminUser -> {
                    if (isUserProfileAlarmaActivaAndGrouped(adminUser)) {
                        return sendAlarmaGroupedMailForUser(totesAlarmesAdmin, adminUser);
                    } else {
                        return false;
                    }
                }).count();
            } catch (UserInformationHelper.UserInformationException e) {
                log.warn("[EML] Error obtenint usuaris administradors per LDAP per a enviament agrupat; fallback als creadors de les alarmes: {}",
                        e.getMessage());
                adminMailCount = totesAlarmesAdmin.stream()
                        .map(a -> a.getAlarmaConfig().getCreatedBy())
                        .filter(Objects::nonNull)
                        .distinct()
                        .filter(this::isUserProfileAlarmaActivaAndGrouped)
                        .filter(createdBy -> sendAlarmaGroupedMailForUser(totesAlarmesAdmin, createdBy))
                        .count();
            }
        }

        // Envia les alarmes dels usuaris no administradors (activades + finalitzades)
		List<String> usuarisActivades = alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfterAndDataEnviamentIsNull(dataDesde);
		List<String> usuarisFinalitzades = alarmaRepository.findDistinctAlarmaConfigCreatedByNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(dataDesde);
		List<String> usuaris = mergeUsuaris(usuarisActivades, usuarisFinalitzades);
		long userMailCount = usuaris.stream().filter(u -> {
			List<AlarmaEntity> alarmesPendentsUser = alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(
					u,
					dataDesde);
			List<AlarmaEntity> alarmesFinalitzadesUser = alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndAlarmaConfigNotificacioFinalitzadaTrueAndDataFinalitzacioAfter(
					u,
					dataDesde);
			return sendAlarmaGroupedMailForUser(mergeAlarmes(alarmesPendentsUser, alarmesFinalitzadesUser), u);
		}).count();
		return adminMailCount + userMailCount;
	}

	private void sendAlarmaMailForUser(
			AlarmaEntity alarma,
			String username,
			AlarmaMailEventType tipusEvent) {
		MonitorAlarmes monitor = new MonitorAlarmes(
				alarma.getEntornAppId(),
				MonitorAlarmes.ENVIAMENT_CORREU_USUARI,
				null,
				username,
				alarmaClientHelper);
		monitor.startAction();
		boolean emailAddressAutoGenerated = false;
		try {
			Usuari usuari = userInformationHelper.usuariFindByUsername(username);
			String email = getMailFromUsuari(usuari);
			if (email == null) {
				emailAddressAutoGenerated = true;
				email = generateMailFromUsuariCodi(usuari);
			}
			monitor.getMonitor().setUrl(email);

			boolean sent = sendAlarmaMail(
					email,
					usuari.getNom(),
					generateIndividualAlarmaSubject(alarma, tipusEvent),
					generateAlarmaBodyMessage(alarma)
			);
			if (sent) {
				monitor.endAction();
				log.debug("[EML] Correu enviat a {} ({})", usuari.getNom(), email);
				if (isLogActivacio()) {
					log.info("[EML] Correu enviat correctament a {} ({}) configId={}{}",
							usuari.getNom(), email, alarma.getAlarmaConfig().getId(),
							emailAddressAutoGenerated ? " [email autogenerat]" : "");
				}
			} else {
				String motiu = emailAddressAutoGenerated ? "No s'ha pogut enviar el correu d'alarma amb email autogenerat" : "No s'ha pogut enviar el correu d'alarma";
				monitor.endAction(new IllegalStateException(motiu), motiu);
				log.warn("[EML] No s'ha pogut enviar el correu a {} ({}) configId={}", usuari.getNom(), email, alarma.getAlarmaConfig().getId());
			}
		} catch (Exception ex) {
			monitor.endAction(ex, emailAddressAutoGenerated ? "Error enviant correu d'alarma a usuari amb email autogenerat" : "Error enviant correu d'alarma a usuari");
			log.error("[EML] No s'ha pogut enviar missatge d'alarma a {} configId={}", username, alarma.getAlarmaConfig().getId(), ex);
		}
	}

	private boolean sendAlarmaGroupedMailForUser(
			List<AlarmaEntity> alarmes,
			String username) {
		MonitorAlarmes monitor = new MonitorAlarmes(
				alarmes != null && !alarmes.isEmpty() ? alarmes.get(0).getEntornAppId() : null,
				MonitorAlarmes.ENVIAMENT_CORREU_AGRUPAT,
				null,
				username,
				alarmaClientHelper);
		boolean emailAddressAutoGenerated = false;
		try {
			if (alarmes == null || alarmes.isEmpty()) {
				return false;
			}
			monitor.startAction();
			if (isUserProfileAlarmaActivaAndGrouped(username)) {
				Usuari usuari = userInformationHelper.usuariFindByUsername(username);
				String email = getMailFromUsuari(usuari);
				if (email == null) {
					emailAddressAutoGenerated = true;
					email = generateMailFromUsuariCodi(usuari);
				}
				monitor.getMonitor().setUrl(email);

				boolean sended = sendAlarmaMail(
						email,
						usuari.getNom(),
						"[COMANDA] Resum diari d'alarmes",
						getAlarmesGroupedText(alarmes));
				if (sended) {
					monitor.endAction();
				} else {
					monitor.endAction(new IllegalStateException("No s'ha pogut enviar el resum d'alarmes"),
							emailAddressAutoGenerated ? "No s'ha pogut enviar el resum d'alarmes amb email autogenerat" : "No s'ha pogut enviar el resum d'alarmes");
				}
				log.debug("[EML] Alarma agrupada: Enviat correu a {} amb email {}, amb {} alarmes activades", usuari.getNom(), email, alarmes.size());
				return sended;
			}
		} catch (Exception ex) {
			monitor.endAction(ex, emailAddressAutoGenerated ? "Error enviant resum d'alarmes amb email autogenerat" : "Error enviant resum d'alarmes");
			log.error("[EML] No s'ha pogut enviar missatge d'alarma", ex);
		}
		return false;
	}

	private List<AlarmaEntity> mergeAlarmes(List<AlarmaEntity> activades, List<AlarmaEntity> finalitzades) {
		Set<Long> ids = new LinkedHashSet<>();
		List<AlarmaEntity> result = new ArrayList<>();
		Stream.concat(activades.stream(), finalitzades.stream()).forEach(a -> {
			if (ids.add(a.getId())) {
				result.add(a);
			}
		});
		return result;
	}

	private List<String> mergeUsuaris(List<String> usuaris1, List<String> usuaris2) {
		Set<String> set = new LinkedHashSet<>(usuaris1);
		set.addAll(usuaris2);
		return new ArrayList<>(set);
	}

	private String getAlarmesGroupedText(List<AlarmaEntity> alarmes) {
		return alarmes.stream()
				.map(this::generateAlarmaBodyMessage)
				.collect(Collectors.joining("\n\n"));
	}

	private boolean isLogActivacio() {
		return Boolean.TRUE.equals(parametresHelper.getParametreBoolean(BaseConfig.PROP_ALARMA_LOG_ACTIVACIO, false));
	}

	private boolean isUserProfileAlarmaActiva(Usuari user) {
		if (user == null)
			return false;
		// Els usuaris definits a PROP_HTTPAUTH_USERNAME o PROP_STATS_AUTH_USER tenen els correus deshabilitats per petició de l'usuari
		if (httpAuthUsername != null && httpAuthUsername.equals(user.getCodi())) return false;
		if (statsAuthUsername != null && statsAuthUsername.equals(user.getCodi())) return false;
		return user.isAlarmaMail();
	}

	private boolean isUserProfileAlarmaActivaAndGrouped(String username) {
		Usuari user = userInformationHelper.usuariFindByUsername(username);
		return isUserProfileAlarmaActiva(user) && user.isAlarmaMailAgrupar();
	}

	/**
	 * Recupera el correu electrònic a usar d'acord amb un Usuari, respectant l'email de preferencia establert
	 */
	private String getMailFromUsuari(Usuari usuari) {
		if (Strings.isNotBlank(usuari.getEmailAlternatiu())) return usuari.getEmailAlternatiu();
		if (Strings.isNotBlank(usuari.getEmail())) return usuari.getEmail();
		return null;
	}

	private String generateMailFromUsuariCodi(Usuari usuari) {
		String defaultDomainName = parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_DEFAULT_DOMAIN, "caib.es");
		return usuari.getCodi() + "@" + defaultDomainName;
	}

	private boolean sendAlarmaMail(String toMail, String toName, String subject, String text) throws MessagingException, UnsupportedEncodingException {
		return mailHelper.sendSimple(
				getAlarmaMailFromAddress(),
				getAlarmaMailFromName(),
				toMail,
				toName,
				subject,
				text);
	}

	private String getAlarmaMailFromAddress() {
		return parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_FROM_ADDRESS, "comanda@caib.es");
	}
	private String getAlarmaMailFromName() {
		return parametresHelper.getParametreText(BaseConfig.PROP_ALARMA_MAIL_FROM_NAME, "Comanda");
	}

}
