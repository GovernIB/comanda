package es.caib.comanda.back.config;

//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.ServerAddress;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import org.springframework.boot.autoconfigure.mongo.MongoProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.MongoDatabaseFactory;
//import org.springframework.data.mongodb.MongoTransactionManager;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración de los componentes de persistencia per MongoDb.
 * 
 * @author Límit Tecnologies
 */
//@Configuration
//@EnableMongoRepositories(
//		basePackages = "es.caib.comanda.estadistica.persist.repository.mongo",
//		mongoTemplateRef = "mongoTemplate"
//)
public class BackMongoPersistenceConfig {
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
//	@Bean
//	public MongoClient mongoClient(MongoProperties properties) {
//		MongoClientSettings settings = MongoClientSettings.builder()
//				.applyConnectionString(new ConnectionString(properties.determineUri()))
//				.applyToClusterSettings(builder ->
//						builder.hosts(Arrays.asList(new ServerAddress("localhost", 27017))))
//				.build();
//
//		return MongoClients.create(settings);
//	}
}
