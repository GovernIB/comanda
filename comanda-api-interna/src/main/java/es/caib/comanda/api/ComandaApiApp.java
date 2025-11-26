package es.caib.comanda.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Classe principal del microservei comanda-back.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@SpringBootApplication
public class ComandaApiApp extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ComandaApiApp.class, args);
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		try {
			Manifest manifest = new Manifest(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
			Attributes attributes = manifest.getMainAttributes();
			String version = attributes.getValue("Implementation-Version");
			String buildTimestamp = attributes.getValue("Build-Timestamp");
			log.info("Carregant l'aplicació comanda-api-interna versió " + version + " generada en data " + buildTimestamp);
		} catch (IOException ex) {
			throw new ServletException("Couldn't read MANIFEST.MF", ex);
		}
		super.onStartup(servletContext);
	}

}
