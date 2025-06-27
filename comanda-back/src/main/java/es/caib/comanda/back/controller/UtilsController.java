package es.caib.comanda.back.controller;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.back.controller.BaseUtilsController;
import org.keycloak.KeycloakPrincipal;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Controller amb les utilitats.
 * 
 * @author Límit Tecnologies
 */
@RestController
public class UtilsController extends BaseUtilsController {

	protected boolean isReactAppMappedFrontProperty(String propertyName) {
		return propertyName.startsWith(BaseConfig.PROPERTY_PREFIX_FRONT) && BaseConfig.REACT_APP_PROPS_MAP.containsKey(propertyName);
	}
	protected String getReactAppMappedFrontProperty(String propertyName) {
		return BaseConfig.REACT_APP_PROPS_MAP.get(propertyName);
	}
	protected boolean isViteMappedFrontProperty(String propertyName) {
		return propertyName.startsWith(BaseConfig.PROPERTY_PREFIX_FRONT) && BaseConfig.VITE_PROPS_MAP.containsKey(propertyName);
	}
	protected String getViteMappedFrontProperty(String propertyName) {
		return BaseConfig.VITE_PROPS_MAP.get(propertyName);
	}

	@Override
	protected String getAuthToken() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			throw new IllegalStateException("No current request attributes found");
		}
		HttpServletRequest request = attrs.getRequest();
		Principal principal = request.getUserPrincipal();
		if (principal instanceof KeycloakPrincipal) {
			KeycloakPrincipal<?> keycloakPrincipal = ((KeycloakPrincipal<?>)request.getUserPrincipal());
			return keycloakPrincipal.getKeycloakSecurityContext().getIdTokenString();
		} else {
			return null;
		}
	}

}
