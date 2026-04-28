package es.caib.comanda.alarmes.logic.helper;

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

//	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_FROM_ADDRESS + ":#{null}}")
//	private String alarmaMailFromAddress;
//	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_FROM_NAME + ":#{null}}")
//	private String alarmaMailFromName;

	private final AlarmaClientHelper alarmaClientHelper;
	private final MailHelper mailHelper;
	private final UserInformationHelper userInformationHelper;
	private final AlarmaRepository alarmaRepository;
	private final ParametresHelper parametresHelper;


	private String generateIndividualAlarmaSubject(AlarmaEntity alarma) {
		boolean alarmaFinalitzada = Objects.nonNull(alarma.getDataFinalitzacio());
		return "[COMANDA] Alarma "+ (alarmaFinalitzada ? "finalitzada" : "activada") + (Strings.isNotBlank(alarma.getAlarmaConfig().getNom()) ? ": " + alarma.getAlarmaConfig().getNom() : "");
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

	public void sendAlarmaGeneric(AlarmaEntity alarma) {
        EntornApp alarmaEntornApp = alarmaClientHelper.entornAppFindById(alarma.getEntornAppId());
		if (alarmaEntornApp == null || Strings.isEmpty(alarmaEntornApp.getAlarmesEmail())) {
			return;
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
					generateIndividualAlarmaSubject(alarma),
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

	public void sendAlarmaUser(AlarmaEntity alarma) {
		if (alarma.getAlarmaConfig().isAdmin()) {
			log.debug("Enviat correu d'alarma per administrador.");
			String[] adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
			Arrays.stream(adminUsers).forEach(adminUser -> {
				if (isUserProfileAlarmaActivaAndUngrouped(adminUser)) {
					sendAlarmaMailForUser(alarma, adminUser);
				} else {
					log.debug("[EML] No s'ha enviat el correu a l'administrador {} degut a que no té actiu l'enviament de correu, o el té configurat com a agrupat.");
				}
			});
		} else {
			log.debug("Enviat correu d'alarma per usuari.");
			String username = alarma.getAlarmaConfig().getCreatedBy();
			if (isUserProfileAlarmaActivaAndUngrouped(username)) {
				sendAlarmaMailForUser(alarma, username);
			} else {
				log.debug("[EML] No s'ha enviat el correu a l'usuari {} degut a que no té actiu l'enviament de correu, o el té configurat com a agrupat.");
			}
		}
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
                log.error("[EML] Error recuperant usuaris administradors. No s'enviaran els correus de les alarmes agrupades pels administradors.");
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
			String username) {
		MonitorAlarmes monitor = new MonitorAlarmes(
				alarma.getEntornAppId(),
				MonitorAlarmes.ENVIAMENT_CORREU_USUARI,
				null,
				username,
				alarmaClientHelper);
		monitor.startAction();

		try {
			Usuari usuari = userInformationHelper.usuariFindByUsername(username);
			String email = getMailFromUsuari(usuari);
			monitor.getMonitor().setUrl(email);

			boolean sent = sendAlarmaMail(
					email,
					usuari.getNom(),
					generateIndividualAlarmaSubject(alarma),
					generateAlarmaBodyMessage(alarma)
			);
			if (sent) {
				monitor.endAction();
			} else {
				monitor.endAction(new IllegalStateException("No s'ha pogut enviar el correu d'alarma"),
						"No s'ha pogut enviar el correu d'alarma");
			}
			log.debug("[EML] Alarma per usuari: Enviat correu a {} amb email {}, amb alarma activada: {}", usuari.getNom(), email, alarma.getAlarmaConfig().getMissatge());
		} catch (Exception ex) {
			monitor.endAction(ex, "Error enviant correu d'alarma a usuari");
			log.error("[EML] No s'ha pogut enviar missatge d'alarma", ex);
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
		try {
			if (alarmes == null || alarmes.isEmpty()) {
				return false;
			}
			monitor.startAction();
			if (isUserProfileAlarmaActivaAndGrouped(username)) {
				Usuari usuari = userInformationHelper.usuariFindByUsername(username);
				String email = getMailFromUsuari(usuari);
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
							"No s'ha pogut enviar el resum d'alarmes");
				}
				log.debug("[EML] Alarma agrupada: Enviat correu a {} amb email {}, amb {} alarmes activades", usuari.getNom(), email, alarmes.size());
				return sended;
			}
		} catch (Exception ex) {
			monitor.endAction(ex, "Error enviant resum d'alarmes");
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

	private boolean isUserProfileAlarmaActivaAndGrouped(String username) {
		Usuari user = userInformationHelper.usuariFindByUsername(username);
		return user != null && user.isAlarmaMail() && user.isAlarmaMailAgrupar();
	}

	private boolean isUserProfileAlarmaActivaAndUngrouped(String username) {
		Usuari user = userInformationHelper.usuariFindByUsername(username);
		return user != null && user.isAlarmaMail() && !user.isAlarmaMailAgrupar();
	}

	/**
	 * Recupera el correu electrònic a usar d'acord amb un Usuari, respectant l'email de preferencia establert
	 */
	private String getMailFromUsuari(Usuari usuari) {
		return Strings.isNotBlank(usuari.getEmailAlternatiu())
				? usuari.getEmailAlternatiu()
				: usuari.getEmail();
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
