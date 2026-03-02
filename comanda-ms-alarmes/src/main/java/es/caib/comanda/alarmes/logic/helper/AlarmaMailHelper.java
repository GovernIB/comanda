package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
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
import java.util.Arrays;
import java.util.List;

/**
 * Enviament de correus d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaMailHelper {

	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_FROM_ADDRESS + ":#{null}}")
	private String alarmaMailFromAddress;
	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_FROM_NAME + ":#{null}}")
	private String alarmaMailFromName;

	private final AlarmaClientHelper alarmaClientHelper;
	private final MailHelper mailHelper;
	private final UserInformationHelper userInformationHelper;
	private final AlarmaRepository alarmaRepository;

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
					alarma.getMissatge()
			);
		} catch (MessagingException | UnsupportedEncodingException e) {
			log.error("Error enviant correu d'alarma genèrica", e);
		}
	}

	public void sendAlarmaUser(AlarmaEntity alarma) {
		if (alarma.getAlarmaConfig().isAdmin()) {
			String[] adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
			Arrays.stream(adminUsers).forEach(a -> {
				if (isUserProfileAlarmaActiva(a)) {
					sendAlarmaMailForUser(alarma, a);
				}
			});
		} else {
			String username = alarma.getAlarmaConfig().getCreatedBy();
			if (isUserProfileAlarmaActiva(username)) {
				sendAlarmaMailForUser(alarma, username);
			}
		}
	}

	public long sendAlarmesAgrupades() {
		// Envia les alarmes dels administradors
		LocalDateTime dataDesde = LocalDateTime.now().minusHours(24);
		List<AlarmaEntity> alarmesPendentsAdmin = alarmaRepository.findByAlarmaConfigAdminTrueAndDataActivacioAfterAndDataEnviamentIsNull(
				dataDesde);
		String[] adminUsers = userInformationHelper.findByRole(BaseConfig.ROLE_ADMIN);
		long adminMailCount = Arrays.stream(adminUsers).filter(a -> {
			if (isUserProfileAlarmaActiva(a)) {
				return sendAlarmaGroupedMailForUser(alarmesPendentsAdmin, a);
			} else {
				return false;
			}
		}).count();
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
					usuari.getEmail(),
					usuari.getNom(),
					"[COMANDA] Alarma activada: " + alarma.getAlarmaConfig().getNom(),
					alarma.getMissatge());
		} catch (Exception ex) {
			log.error("No s'ha pogut enviar missatge d'alarma", ex);
		}
	}

	private boolean sendAlarmaGroupedMailForUser(
			List<AlarmaEntity> alarmes,
			String username) {
		try {
			if (isUserProfileAlarmaActiva(username)) {
				Usuari usuari = userInformationHelper.usuariFindByUsername(username);
				return sendAlarmaMail(
						usuari.getEmail(),
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
		StringBuilder sb = new StringBuilder();
		alarmes.forEach(a -> {
			sb.append("\t ").append(a.getMissatge());
		});
		return sb.toString();
	}

	private boolean isUserProfileAlarmaActiva(String username) {
        Usuari user = userInformationHelper.usuariFindByUsername(username);
		return user.isAlarmaMail();
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
