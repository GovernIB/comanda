package es.caib.comanda.client.config;

import es.caib.comanda.client.*;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Configuració per a Open Feign.
 *
 * @author Límit Tecnologies
 */
@Configuration
@EnableFeignClients(clients = {
		AclServiceClient.class,
		AppServiceClient.class,
		EntornServiceClient.class,
		EntornAppServiceClient.class,
		MonitorServiceClient.class,
        SalutServiceClient.class,
        ParametreServiceClient.class,
        TascaServiceClient.class,
        AvisServiceClient.class,
        PermisServiceClient.class,
        UsuariServiceClient.class
})
public class OpenFeignConfig {

	@Bean
	public RequestInterceptor headerInterceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate template) {
				ServletRequestAttributes attrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
				if (attrs != null) {
					Cookie[] cookies = attrs.getRequest().getCookies();
					if (cookies != null && cookies.length > 0) {
						String cookieHeader = Arrays.stream(cookies).
								map(cookie -> cookie.getName() + "=" + cookie.getValue()).
								collect(Collectors.joining("; "));
						template.header("Cookie", cookieHeader);
					}
				}
			}
		};
	}

	@Bean
	public Request.Options feignRequestOptions() {
		return new Request.Options(5, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true);
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
