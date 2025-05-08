package es.caib.comanda.estadistica.persist.config;

//import com.mongodb.client.MongoClients;
//import org.springframework.boot.autoconfigure.mongo.MongoProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.mongodb.MongoDatabaseFactory;
//import org.springframework.data.mongodb.MongoTransactionManager;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración de los componentes de persistencia.
 * 
 * @author Límit Tecnologies
 */
//@Configuration
//@Profile("!back")
//@EnableMongoRepositories(
//		basePackages = "es.caib.comanda.estadistica.persist.repository.mongo",
//		mongoTemplateRef = "mongoTemplate"
//)
public class EstadisticaMongoPersistenceConfig {
//
//	@Bean
//	public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory) {
//		return new MongoTemplate(mongoDbFactory);
//	}
//
//	@Bean
//	public MongoDatabaseFactory mongoDatabaseFactory(MongoProperties properties) {
//		return new SimpleMongoClientDatabaseFactory(
//				MongoClients.create(properties.determineUri()),
//				properties.getDatabase()
//		);
//	}
//
//	@Bean
//	public PlatformTransactionManager mongoTransactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
//		return new MongoTransactionManager(mongoDatabaseFactory);
//	}
//
}
