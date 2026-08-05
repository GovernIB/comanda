package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import lombok.Getter;
import org.fundaciobit.pluginsib.userinformation.ldap.LdapUserInformationPlugin;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Properties;

import static es.caib.comanda.base.config.BaseConfig.PROPS_LDAP;
import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.USUARI_CACHE;

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
    private final AlarmaClientHelper alarmaClientHelper;

    public UserInformationHelper(
        UsuariServiceClient usuariServiceClient,
        HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
        AlarmaClientHelper alarmaClientHelper,
        Environment environment
    ) {
        this.usuariServiceClient = usuariServiceClient;
        this.httpAuthorizationHeaderHelper = httpAuthorizationHeaderHelper;
        this.alarmaClientHelper = alarmaClientHelper;
        var properties = new Properties();
        for (String property : PROPS_LDAP) {
            String value = environment.getProperty(property);
            if (value != null) {
                properties.put(property, value);
            }
        }
        this.userInformationPlugin = new LdapUserInformationPlugin("es.caib.comanda.", properties);
    }

    @Cacheable(value = USUARI_CACHE, key = "#username")
    public Usuari usuariFindByUsername(String username) {
        MonitorUserInformation monitor = new MonitorUserInformation(
            MonitorUserInformation.FIND_BY_USERNAME,
            username,
            alarmaClientHelper);
        monitor.startAction();
        try {
            EntityModel<Usuari> usuari;
            try {
                usuari = usuariServiceClient.getOneByCodiInternal(
                    username,
                    httpAuthorizationHeaderHelper.getAuthorizationHeader());
            } catch (FeignException.NotFound e) {
                monitor.endAction("Usuari no trobat: " + username);
                return null;
            }
            Usuari result = usuari != null ? usuari.getContent() : null;
            if (result == null) {
                monitor.endAction("La resposta no conté dades per a l'usuari: " + username);
            } else {
                monitor.endAction();
            }
            return result;
        } catch (Exception ex) {
            monitor.endAction(ex, "Error cercant usuari per nom d'usuari");
            throw ex;
        }
    }

    public String[] findByRole(String role) {
        MonitorUserInformation monitor = new MonitorUserInformation(
            MonitorUserInformation.FIND_BY_ROLE,
            role,
            alarmaClientHelper);
        monitor.startAction();
        try {
            String[] result = userInformationPlugin.getUsernamesByRol(role);
            if (result == null || result.length == 0) {
                monitor.endAction("No s'han trobat usuaris per al rol: " + role);
            } else {
                monitor.endAction();
            }
            return result;
        } catch (Exception ex) {
            monitor.endAction(ex, "Error cercant usuaris per rol");
            throw new UserInformationException(
                "getUsernamesByRol",
                new String[]{role},
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
