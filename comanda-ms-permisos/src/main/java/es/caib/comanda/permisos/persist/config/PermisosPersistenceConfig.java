package es.caib.comanda.permisos.persist.config;

import es.caib.comanda.ms.persist.config.BasePersistenceConfig;
import es.caib.comanda.ms.persist.repository.BaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración de los componentes de persistencia.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
@EnableJpaRepositories(
		basePackages = { "es.caib.comanda.ms.persist", "es.caib.comanda.permisos.persist" },
		entityManagerFactoryRef = "mainEntityManager",
		transactionManagerRef = "mainTransactionManager",
		repositoryBaseClass = BaseRepositoryImpl.class
)
public class PermisosPersistenceConfig extends BasePersistenceConfig {

	protected String[] getEntityPackages() {
		return new String [] {
				"es.caib.comanda.ms.persist",
				"es.caib.comanda.permisos.persist"
		};
	}

}
