package es.caib.comanda.configuracio.logic.intf.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Utilitats per a obtenir informació de la petició HTTP.
 * 
 * @author Limit Tecnologies
 */
public class HttpRequestUtil {

	public static Optional<HttpServletRequest> getCurrentHttpRequest() {
		return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).
				filter(ServletRequestAttributes.class::isInstance).
				map(ServletRequestAttributes.class::cast).
				map(ServletRequestAttributes::getRequest);
	}

}
