package es.caib.comanda.back;

import es.caib.comanda.ms.back.intf.HandlerInterceptorWithPath;
import es.caib.comanda.usuaris.logic.helper.UsuarisRefreshHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserHandlerInterceptor implements HandlerInterceptorWithPath {

	private final UsuarisRefreshHelper usuarisRefreshHelper;

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                usuarisRefreshHelper.refreshCurrentUser();
            } catch (Exception e) {
                log.error("Error refreshing current user", e);
            }
        }
		return true;
	}

	@Override
	public String getPath() {
		return "/**";
	}

}
