package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.Getter;
import org.fundaciobit.pluginsib.userinformation.ldap.LdapUserInformationPlugin;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Properties;

import static es.caib.comanda.base.config.BaseConfig.PROPS_LDAP;

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

	public UserInformationHelper(
			UsuariServiceClient usuariServiceClient,
			HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
			Environment environment
	) {
		this.usuariServiceClient = usuariServiceClient;
		this.httpAuthorizationHeaderHelper = httpAuthorizationHeaderHelper;
		var properties = new Properties();
		for (String property : PROPS_LDAP) {
			String value = environment.getProperty(property);
            if (value != null) {
                properties.put(property, value);
            }
        }
		this.userInformationPlugin = new LdapUserInformationPlugin("es.caib.comanda.", properties);
	}

	public Usuari usuariFindByUsername(String username) {
		EntityModel<Usuari> usuari = usuariServiceClient.getOneByCodiInternal(
				username,
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		return usuari != null ? usuari.getContent() : null;
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
