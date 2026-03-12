package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.Getter;
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
	private final UsuariServiceClient usuariServiceClient;
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

	public UserInformationHelper(UsuariServiceClient usuariServiceClient, HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper) {
        this.usuariServiceClient = usuariServiceClient;
        this.httpAuthorizationHeaderHelper = httpAuthorizationHeaderHelper;
        this.userInformationPlugin = new LdapUserInformationPlugin("");
	}

	public Usuari usuariFindByUsername(String username) {
		PagedModel<EntityModel<Usuari>> usuaris = usuariServiceClient.find(
				null,
				"codi:'" + username + "'",
				null,
				null,
				"0",
				1,
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		if (usuaris == null) return null;
		return usuaris.getContent().stream().
				findFirst().
				map(EntityModel::getContent).
				orElse(null);
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
