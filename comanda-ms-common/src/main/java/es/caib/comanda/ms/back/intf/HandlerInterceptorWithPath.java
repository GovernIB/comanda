package es.caib.comanda.ms.back.intf;

import org.springframework.web.servlet.HandlerInterceptor;

public interface HandlerInterceptorWithPath extends HandlerInterceptor {
    String getPath();
}
