package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

	private final MailHelper mailHelper;
	private final UserInformationHelper userInformationHelper;
	private final AlarmaRepository alarmaRepository;

	public void sendAlarma(AlarmaEntity alarma) {
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

	public void sendAlarmaAgrupacio(String username) {
		if (isUserProfileAlarmaActiva(username)) {
			List<AlarmaEntity> alarmesPendents = alarmaRepository.findByAlarmaConfigCreatedByAndDataEnviamentIsNull(username);
			if (isUserAdmin(username)) {
				alarmesPendents.addAll(
						alarmaRepository.findByAlarmaConfigAdminAndDataEnviamentIsNull(true));
			}
			sendAlarmaGroupedMailForUser(alarmesPendents, username);
		}
	}

	private void sendAlarmaMailForUser(
			AlarmaEntity alarma,
			String username) {
		try {
			UserInformationHelper.UserInformation userInformation = userInformationHelper.getUserInfo(username);
			mailHelper.sendSimple(
					"comanda@caib.es",
					"Comanda",
					userInformation.getEmail(),
					userInformation.getFullName(),
					"[COMANDA] Alarma activada: " + alarma.getMissatge(),
					"S'ha produït una alarma!");
		} catch (Exception ex) {
			log.error("No s'ha pogut enviar missatge d'alarma", ex);
		}
	}

	private void sendAlarmaGroupedMailForUser(
			List<AlarmaEntity> alarmes,
			String username) {
		try {
			if (isUserProfileAlarmaActiva(username)) {
				UserInformationHelper.UserInformation userInformation = userInformationHelper.getUserInfo(username);
				mailHelper.sendSimple(
						"comanda@caib.es",
						"Comanda",
						userInformation.getEmail(),
						userInformation.getFullName(),
						"[COMANDA] Resum diari d'alarmes activades",
						getAlarmesGroupedText(alarmes));
			}
		} catch (Exception ex) {
			log.error("No s'ha pogut enviar missatge d'alarma", ex);
		}
	}

	private String getAlarmesGroupedText(List<AlarmaEntity> alarmes) {
		StringBuilder sb = new StringBuilder();
		alarmes.forEach(a -> {
			sb.append("\t ").append(a.getMissatge());
		});
		return sb.toString();
	}

	private boolean isUserAdmin(String username) {
		String[] roles = userInformationHelper.getRolesByUsername(username);
		if (roles != null) {
			return Arrays.asList(roles).contains(BaseConfig.ROLE_ADMIN);
		} else {
			return false;
		}
	}

	private boolean isUserProfileAlarmaActiva(String username) {
		return true;
	}

}
