package es.caib.comanda.back;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

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
@ComponentScan(
		basePackages = {
				"es.caib.comanda.ms",
				"es.caib.comanda.configuracio",
				"es.caib.comanda.salut",
				"es.caib.comanda.back"
		},
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.REGEX,
				pattern = {
						"es\\.caib\\.comanda\\.configuracio\\.back\\.config\\..*",
						"es\\.caib\\.comanda\\.configuracio\\.persist\\.config\\..*",
						"es\\.caib\\.comanda\\.salut\\.back\\.config\\..*",
						"es\\.caib\\.comanda\\.salut\\.persist\\.config\\..*"
				})
)
public class ComandaBackApp extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ComandaBackApp.class, args);
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		try {
			Manifest manifest = new Manifest(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
			Attributes attributes = manifest.getMainAttributes();
			String version = attributes.getValue("Implementation-Version");
			String buildTimestamp = attributes.getValue("Build-Timestamp");
			log.info("Carregant l'aplicació comanda-back versió " + version + " generada en data " + buildTimestamp);
		} catch (IOException ex) {
			throw new ServletException("Couldn't read MANIFEST.MF", ex);
		}
		super.onStartup(servletContext);
	}

}
