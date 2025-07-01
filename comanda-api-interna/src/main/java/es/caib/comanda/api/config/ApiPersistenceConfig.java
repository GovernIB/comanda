package es.caib.comanda.api.config;

import es.caib.comanda.ms.persist.config.BasePersistenceConfig;
import es.caib.comanda.ms.persist.repository.BaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración de los componentes de persistencia.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@EnableJpaRepositories(
		basePackages = {
				"es.caib.comanda.ms.persist",
				"es.caib.comanda.configuracio.persist",
				"es.caib.comanda.salut.persist",
				"es.caib.comanda.estadistica.persist",
				"es.caib.comanda.monitor.persist"
		},
		entityManagerFactoryRef = "mainEntityManager",
		transactionManagerRef = "mainTransactionManager",
		repositoryBaseClass = BaseRepositoryImpl.class
)
public class ApiPersistenceConfig extends BasePersistenceConfig {

	protected String[] getEntityPackages() {
		return new String [] {
				"es.caib.comanda.ms.persist",
				"es.caib.comanda.configuracio.persist",
				"es.caib.comanda.salut.persist",
				"es.caib.comanda.estadistica.persist",
				"es.caib.comanda.monitor.persist"
		};
	}

}
