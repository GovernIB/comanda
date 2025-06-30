package es.caib.comanda.client.config;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Configuració per a Open Feign.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableFeignClients(clients = {
		AppServiceClient.class,
		EntornServiceClient.class,
		EntornAppServiceClient.class,
		MonitorServiceClient.class,
		SalutServiceClient.class,
		EstadisticaServiceClient.class
})
public class OpenFeignConfig {

	private static final String JSESSIONID_COOKIE_NAME = "JSESSIONID";

	@Bean
	public RequestInterceptor headerInterceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
				if (attrs != null) {
					HttpServletRequest request = attrs.getRequest();
					Cookie[] cookies = request.getCookies();
					if (cookies != null && cookies.length > 0) {
						StringBuilder cookieHeader = new StringBuilder();
						for (int i = 0; i < cookies.length; i++) {
							cookieHeader.append(cookies[i].getName())
									.append("=")
									.append(cookies[i].getValue());
							if (i < cookies.length - 1) {
								cookieHeader.append("; ");
							}
						}
						template.header("Cookie", cookieHeader.toString());
					}
				}
			}
		};
	}

	@Bean
	public Retryer feignRetryer() {
		return new Retryer.Default(100, 1000, 3);
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return new CustomErrorDecoder();
	}
}
