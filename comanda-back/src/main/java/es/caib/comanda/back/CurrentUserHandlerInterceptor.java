package es.caib.comanda.back;

import es.caib.comanda.ms.back.intf.HandlerInterceptorWithPath;
import es.caib.comanda.usuaris.logic.helper.UsuarisAuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class CurrentUserHandlerInterceptor implements HandlerInterceptorWithPath {

	private final UsuarisAuthHelper usuarisAuthHelper;

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
			usuarisAuthHelper.refreshCurrentUser();
		}
		return true;
	}

	@Override
	public String getPath() {
		return "/**";
	}

}
