package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.Usuari;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enviament de correus d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaMailHelper {
	private static final DateTimeFormatter ALARMA_DIA_FORMATTER =
			DateTimeFormatter.ofPattern("dd/MM/yyyy 'a les' HH:mm");

	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_FROM_ADDRESS + ":#{null}}")
	private String alarmaMailFromAddress;
	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_FROM_NAME + ":#{null}}")
	private String alarmaMailFromName;

	private final AlarmaClientHelper alarmaClientHelper;
	private final MailHelper mailHelper;
	private final UserInformationHelper userInformationHelper;
	private final AlarmaRepository alarmaRepository;


	private String generateAlarmaBodyMessage(AlarmaEntity alarma) {
		String nom = alarma.getAlarmaConfig().getNom();
		EntornApp alarmaEntornApp = alarmaClientHelper.entornAppFindById(alarma.getEntornAppId());
		App alarmaApp = alarmaClientHelper.appFindById(alarmaEntornApp.getApp().getId());
		Entorn alarmaEntorn = alarmaClientHelper.entornById(alarmaEntornApp.getEntorn().getId());
		String app = alarmaApp.getNom();
		String entorn = alarmaEntorn.getNom();
		String message = alarma.getMissatge();
		String dataActivacio = alarma.getDataActivacio().format(ALARMA_DIA_FORMATTER);
		String missatgeFinalitzacio = alarma.getDataFinalitzacio() != null ? "\nFinalitzada el " + alarma.getDataFinalitzacio().format(ALARMA_DIA_FORMATTER) : "";

		return "Alarma \"" + nom + "\" activada el " + dataActivacio +
				" per a l'aplicació " + app + " - " + entorn + ":\n" +
				message + missatgeFinalitzacio;
	}

	public void sendAlarmaGeneric(AlarmaEntity alarma) {
        EntornApp alarmaEntornApp = alarmaClientHelper.entornAppFindById(alarma.getEntornAppId());
		if (alarmaEntornApp == null || Strings.isEmpty(alarmaEntornApp.getAlarmesEmail())) {
			return;
		}

		try {
			sendAlarmaMail(
					alarmaEntornApp.getAlarmesEmail(),
					"Correu genèric (" + alarmaEntornApp.getApp().getNom() + " - " + alarmaEntornApp.getEntorn().getNom() + ")",
					"[COMANDA] Alarma activada: " + alarma.getAlarmaConfig().getNom(),
					generateAlarmaBodyMessage(alarma)
			);
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.error("Error enviant correu d'alarma genèrica", e);
		}
	}

	public void sendAlarmaUser(AlarmaEntity alarma) {
		if (alarma.getAlarmaConfig().isAdmin()) {
			String[] adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
			Arrays.stream(adminUsers).forEach(adminUser -> {
				if (isUserProfileAlarmaActivaAndUngrouped(adminUser)) {
					sendAlarmaMailForUser(alarma, adminUser);
				}
			});
		} else {
			String username = alarma.getAlarmaConfig().getCreatedBy();
			if (isUserProfileAlarmaActivaAndUngrouped(username)) {
				sendAlarmaMailForUser(alarma, username);
			}
		}
	}

	public long sendAlarmesAgrupades() {
		// Envia les alarmes dels administradors
		LocalDateTime dataDesde = LocalDateTime.now().minusHours(24);
		List<AlarmaEntity> alarmesPendentsAdmin = alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(
				dataDesde);
        long adminMailCount = 0;
        try {
            String[] adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
            adminMailCount = Arrays.stream(adminUsers).filter(adminUser -> {
                if (isUserProfileAlarmaActivaAndGrouped(adminUser)) {
                    return sendAlarmaGroupedMailForUser(alarmesPendentsAdmin, adminUser);
                } else {
                    return false;
                }
            }).count();
        } catch (UserInformationHelper.UserInformationException e) {
            log.error("Error recuperant usuaris administradors. No s'enviaran els correus de les alarmes agrupades pels administradors.");
        }
        // Envia les alarmes dels usuaris no administradors
		List<String> usuaris = alarmaRepository.findDistinctAlarmaConfigCreatedByDataActivacioAfter(dataDesde);
		long userMailCount = usuaris.stream().filter(u -> {
			List<AlarmaEntity> alarmesPendentsUser = alarmaRepository.findByAlarmaConfigAdminFalseAndAlarmaConfigCreatedByAndDataActivacioAfterAndDataEnviamentIsNull(
					u,
					dataDesde);
			return sendAlarmaGroupedMailForUser(alarmesPendentsUser, u);
		}).count();
		return adminMailCount + userMailCount;
	}

	private void sendAlarmaMailForUser(
			AlarmaEntity alarma,
			String username) {
		try {
			Usuari usuari = userInformationHelper.usuariFindByUsername(username);

			sendAlarmaMail(
					getMailFromUsuari(usuari),
					usuari.getNom(),
					"[COMANDA] Alarma activada: " + alarma.getAlarmaConfig().getNom(),
					generateAlarmaBodyMessage(alarma)
			);
		} catch (Exception ex) {
			log.error("No s'ha pogut enviar missatge d'alarma", ex);
		}
	}

	private boolean sendAlarmaGroupedMailForUser(
			List<AlarmaEntity> alarmes,
			String username) {
		try {
			if (isUserProfileAlarmaActivaAndGrouped(username)) {
				Usuari usuari = userInformationHelper.usuariFindByUsername(username);
				return sendAlarmaMail(
						getMailFromUsuari(usuari),
						usuari.getNom(),
						"[COMANDA] Resum diari d'alarmes activades",
						getAlarmesGroupedText(alarmes));
			}
		} catch (Exception ex) {
			log.error("No s'ha pogut enviar missatge d'alarma", ex);
		}
		return false;
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
				alarmaMailFromAddress != null ? alarmaMailFromAddress : "comanda@caib.es",
				alarmaMailFromName != null ? alarmaMailFromName : "Comanda",
				toMail,
				toName,
				subject,
				text);
	}

}
