package es.caib.comanda.monitor.persist.config;

import es.caib.comanda.ms.persist.config.BasePersistenceConfig;
import es.caib.comanda.ms.persist.repository.BaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuració de persistència per a l'ús del mòdul de monitoreig
 * Aquesta classe hereta el comportament configurat en BasePersistenceConfig, afegint configuracions específiques per al monitor.
 *
 * Aquesta configuració només s'aplicarà si el perfil actiu no és "back", segons l'anotació @Profile.
 * Defineix els paquets d'entitats per ser utilitzats pel gestor de persistència i altres configuracions relacionades amb JPA.
 *
 * Anotacions principals:
 * - @Configuration: Marca aquesta classe com una classe de configuració en Spring.
 * - @Profile("!back"): Aquesta configuració està activa per a qualsevol perfil excepte "back".
 * - @EnableJpaRepositories: Activa el suport per a la creació de repositoris JPA.
 *
 * Detalls de les configuracions:
 * - basePackages: Defineix els paquets on es buscaran repositoris JPA.
 * - entityManagerFactoryRef: Especifica la referència al bean d'EntityManagerFactory a utilitzar.
 * - transactionManagerRef: Especifica la referència al bean de TransactionManager a utilitzar.
 * - repositoryBaseClass: Defineix la implementació base dels repositoris.
 *
 * Aquesta classe redefineix els paquets d'entitats a través del mètode abstracte `getEntityPackages`, especificant els paquets rellevants.
 *
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
@EnableJpaRepositories(
		basePackages = { "es.caib.comanda.ms.persist", "es.caib.comanda.monitor.persist" },
		entityManagerFactoryRef = "mainEntityManager",
		transactionManagerRef = "mainTransactionManager",
		repositoryBaseClass = BaseRepositoryImpl.class
)
public class MonitorPersistenceConfig extends BasePersistenceConfig {

	protected String[] getEntityPackages() {
		return new String [] {
				"es.caib.comanda.ms.persist",
				"es.caib.comanda.monitor.persist"
		};
	}

}
