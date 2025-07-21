package es.caib.comanda.avisos.persist.config;

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
		basePackages = { "es.caib.comanda.ms.persist", "es.caib.comanda.avisos.persist" },
		entityManagerFactoryRef = "mainEntityManager",
		transactionManagerRef = "mainTransactionManager",
		repositoryBaseClass = BaseRepositoryImpl.class
)
public class AvisosPersistenceConfig extends BasePersistenceConfig {

	protected String[] getEntityPackages() {
		return new String [] {
				"es.caib.comanda.ms.persist",
				"es.caib.comanda.avisos.persist"
		};
	}

}
