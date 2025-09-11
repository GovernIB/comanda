package es.caib.comanda.back;

import es.caib.comanda.ms.back.intf.HandlerInterceptorWithPath;
import es.caib.comanda.usuaris.logic.intf.service.UsuariService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CurrentUserHandlerInterceptor implements HandlerInterceptorWithPath {

	private final UsuariService usuariService;

	public CurrentUserHandlerInterceptor(UsuariService usuariService) {
		this.usuariService = usuariService;
	}

	@Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//		TODO Si inicien sessió amb Jboss o amb springboot es principal sera de un tipo diferent
//		debuguejar quin tipo de objecte es en cada cas


//		TODO Proves a fer:
//		Autenticació anonymous (comprovar que denega accés), auth amb spring, auth amb jboss
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
			usuariService.refreshCurrentUser();
        }

        return true;
    }

    @Override
    public String getPath() {
        return "/**";
    }
}
