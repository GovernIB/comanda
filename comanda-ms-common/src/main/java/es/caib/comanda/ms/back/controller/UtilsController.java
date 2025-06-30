package es.caib.comanda.ms.back.controller;

import es.caib.comanda.ms.back.config.WebSecurityConfig;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping
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
		ServletRequestAttributes attrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			throw new IllegalStateException("No current request attributes found");
		}
		HttpServletRequest request = attrs.getRequest();
		Principal principal = request.getUserPrincipal();
		if (principal instanceof PreAuthenticatedAuthenticationToken) {
			PreAuthenticatedAuthenticationToken token = ((PreAuthenticatedAuthenticationToken) request.getUserPrincipal());
			if (token.getDetails() instanceof WebSecurityConfig.PreauthWebAuthenticationDetails) {
				WebSecurityConfig.PreauthWebAuthenticationDetails tokenDetails = (WebSecurityConfig.PreauthWebAuthenticationDetails) token.getDetails();
				return tokenDetails.getJwtToken();
			}
		}
		return null;
	}

}
