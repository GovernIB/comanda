package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Salut;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fundaciobit.pluginsib.userinformation.RolesInfo;
import org.fundaciobit.pluginsib.userinformation.UserInfo;
import org.fundaciobit.pluginsib.userinformation.ldap.LdapUserInformationPlugin;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Helper per a consultar informacio dels usuaris utilitzant PluginsIb.
 *
 * @author Límit Tecnologies
 */
@Component
public class UserInformationHelper {

	private final LdapUserInformationPlugin userInformationPlugin;

	public UserInformationHelper(UsuariServiceClient usuariServiceClient) {
		this.userInformationPlugin = new LdapUserInformationPlugin("");
	}

	public UserInformation getUserInfo(String username) {
		try {
			UserInfo userInfo = userInformationPlugin.getUserInfoByUserName(username);
			if (userInfo != null) {
				return new UserInformation(
						userInfo.getUsername(),
						userInfo.getName(),
						getSurnames(userInfo),
						userInfo.getEmail(),
						userInfo.getAdministrationID());
			} else {
				return null;
			}
		} catch (Exception ex) {
			throw new UserInformationException(
					"getUserInfo",
					new String[] { username },
					ex);
		}
	}

	public String[] getRolesByUsername(String username) {
		try {
			RolesInfo rolesInfo = userInformationPlugin.getRolesByUsername(username);
			return rolesInfo != null ? rolesInfo.getRoles() : null;
		} catch (Exception ex) {
			throw new UserInformationException(
					"getRolesByUsername",
					new String[] { username },
					ex);
		}
	}

	public String[] findByRole(String role) {
		try {
			return userInformationPlugin.getUsernamesByRol(role);
		} catch (Exception ex) {
			throw new UserInformationException(
					"getUsernamesByRol",
					new String[] { role },
					ex);
		}
	}

	private String getSurnames(UserInfo userInfo) {
		if (userInfo != null && userInfo.getSurname1() != null) {
			return userInfo.getSurname1() + (userInfo.getSurname2() != null ? " " + userInfo.getSurname2() : "");
		} else {
			return null;
		}
	}

	@Getter
	@AllArgsConstructor
	public static class UserInformation extends RuntimeException {
		private String code;
		private String name;
		private String surnames;
		private String email;
		private String nif;
		public String getFullName() {
			return getName() + (getSurnames() != null ? " " + getSurnames() : "");
		}
	}

	@Getter
	public static class UserInformationException extends RuntimeException {
		private final String method;
		private final String[] params;
		public UserInformationException(
				String method,
				String[] params,
				Throwable t) {
			super("Exception calling user information " + method + " method with params " + Arrays.toString(params), t);
			this.method = method;
			this.params = params;
		}
	}

}
