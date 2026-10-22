package es.caib.comanda.ms.back.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Configuració de Springdoc OpenAPI.
 * Permet configurar autenticació via Bearer token (JWT) o Basic.
 * 
 * @author Límit Tecnologies
 */
@Slf4j
public abstract class BaseOpenApiConfig {

	public enum AuthType {
		BEARER,
		BASIC
	}

	public static final String SECURITY_NAME = "basicAuth";
	public static final String BASIC_SECURITY_SCHEME = "basic";
	public static final String BEARER_SECURITY_SCHEME = "bearer";

	@Bean
	public OpenAPI customOpenAPI() {
		String version = "Unknown";
		try {
			Manifest manifest = new Manifest(getClass().getResourceAsStream("/META-INF/MANIFEST.MF"));
			Attributes attributes = manifest.getMainAttributes();
			version = attributes.getValue("Implementation-Version");
		} catch (IOException ex) {
			log.error("No s'ha pogut obtenir la versió del fitxer MANIFEST.MF", ex);
		}
		OpenAPI openapi = new OpenAPI().info(new Info().title(getTitle()).version(version));
		// TODO l'ordenació d'aquesta forma no acaba d'anar be...
		openapi.setTags(tags());
		if (enableAuthComponent()) {
			if (getAuthType() == AuthType.BASIC) {
				openapi
						.components(new Components().addSecuritySchemes(
								SECURITY_NAME,
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme(BASIC_SECURITY_SCHEME)
										.in(SecurityScheme.In.HEADER)
										.name("Authorization")))
						.addSecurityItem(new SecurityRequirement().addList(SECURITY_NAME, Collections.emptyList()));
			} else {
				openapi
						.components(new Components().addSecuritySchemes(
								"Bearer token",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme(BEARER_SECURITY_SCHEME)
										.bearerFormat("JWT")
										.in(SecurityScheme.In.HEADER)
										.name("Authorization")))
						.addSecurityItem(new SecurityRequirement().addList("Bearer token", Arrays.asList("read", "write")));
			}
		}
		return openapi;
	}

	protected List<Tag> tags() {
		return null;
	}

	/**
	 * Títol a mostrar a la documentació OpenAPI.
	 */
	protected abstract String getTitle();

	/**
	 * Indica si s'ha d'incloure el component de seguretat a l'esquema OpenAPI.
	 */
	protected abstract boolean enableAuthComponent();

	/**
	 * Tipus d'autenticació a emprar quan {@link #enableAuthComponent()} és true.
	 * Per defecte: BEARER.
	 */
	protected AuthType getAuthType() {
		return AuthType.BEARER;
	}
}
