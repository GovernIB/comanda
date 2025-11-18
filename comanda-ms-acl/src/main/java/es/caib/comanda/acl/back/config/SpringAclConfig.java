package es.caib.comanda.acl.back.config;

import es.caib.comanda.base.config.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.ConsoleAuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Configuració de Spring Security ACL com a motor intern.
 */
@Slf4j
//@Configuration
//@Profile("!back")
public class SpringAclConfig {

    @Autowired(required = false)
    private CacheManager cacheManager; // esperem HazelcastCacheManager al projecte

    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        // Autorització bàsica per a ADMIN via rol estàndard; ajustar si cal
        return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }

    @Bean
    public AclCache aclCache(PermissionGrantingStrategy permissionGrantingStrategy,
                             AclAuthorizationStrategy aclAuthorizationStrategy) {
        // Usa el gestor de caché Spring (Hazelcast) amb un nom de caché dedicat
        SpringCacheBasedAclCache cache = new SpringCacheBasedAclCache(
                cacheManager.getCache("springAclCache"),
                permissionGrantingStrategy,
                aclAuthorizationStrategy
        );
        return cache;
    }

    @Bean
    public LookupStrategy lookupStrategy(DataSource dataSource,
                                         AclCache aclCache,
                                         AclAuthorizationStrategy aclAuthorizationStrategy,
                                         PermissionGrantingStrategy permissionGrantingStrategy) {
        return new BasicLookupStrategy(
                dataSource,
                aclCache,
                aclAuthorizationStrategy,
                permissionGrantingStrategy
        );
    }

    @Bean
    public JdbcMutableAclService mutableAclService(DataSource dataSource,
                                                   LookupStrategy lookupStrategy,
                                                   AclCache aclCache) {
        JdbcMutableAclService service = new JdbcMutableAclService(dataSource, lookupStrategy, aclCache);
        configureIdentityQueriesPerDatabase(dataSource, service);
        return service;
    }

    private void configureIdentityQueriesPerDatabase(DataSource dataSource, JdbcMutableAclService service) {
        String dbProduct = null;
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            dbProduct = md.getDatabaseProductName();
        } catch (Exception ex) {
            log.warn("No s'ha pogut detectar el tipus de BD; es mantindran les consultes per defecte d'ACL", ex);
            return;
        }
        String prefix = BaseConfig.DB_PREFIX; // mateix prefix que s'usa a Liquibase (${db_prefix})
        try {
            if (dbProduct != null && dbProduct.toLowerCase().contains("oracle")) {
                // Oracle: fem servir seqüències explícites + triggers (definides a Liquibase)
                service.setSidIdentityQuery("select " + prefix + "acl_sid_seq.currval from dual");
                service.setClassIdentityQuery("select " + prefix + "acl_class_seq.currval from dual");
                setIfAvailable(service, "setObjectIdentityIdentityQuery", "select " + prefix + "acl_object_identity_seq.currval from dual");
                setIfAvailable(service, "setAclEntryIdentityQuery", "select " + prefix + "acl_entry_seq.currval from dual");
                log.info("Spring ACL identity queries configurades per Oracle");
            } else if (dbProduct != null && (dbProduct.toLowerCase().contains("postgres"))) {
                // PostgreSQL: usem la seqüència auto-generada "<taula>_id_seq"
                service.setSidIdentityQuery("select currval(pg_get_serial_sequence('" + prefix + "acl_sid','id'))");
                service.setClassIdentityQuery("select currval(pg_get_serial_sequence('" + prefix + "acl_class','id'))");
                setIfAvailable(service, "setObjectIdentityIdentityQuery", "select currval(pg_get_serial_sequence('" + prefix + "acl_object_identity','id'))");
                setIfAvailable(service, "setAclEntryIdentityQuery", "select currval(pg_get_serial_sequence('" + prefix + "acl_entry','id'))");
                log.info("Spring ACL identity queries configurades per PostgreSQL");
            } else if (dbProduct != null && dbProduct.toLowerCase().contains("h2")) {
                // H2: valor per defecte de la classe (call identity()) ja és vàlid; opcionalment es pot fixar
                // service.setSidIdentityQuery("call identity()");
                // service.setClassIdentityQuery("call identity()");
                log.info("Spring ACL identity queries: H2 (valors per defecte)");
            } else {
                log.info("Spring ACL identity queries: DB desconeguda ('{}'), es mantenen per defecte", dbProduct);
            }
        } catch (Exception ex) {
            log.error("Error configurant les identity queries de Spring ACL per a '{}': {}", dbProduct, ex.getMessage(), ex);
        }
    }

    private void setIfAvailable(JdbcMutableAclService service, String methodName, String query) {
        try {
            service.getClass().getMethod(methodName, String.class).invoke(service, query);
        } catch (NoSuchMethodException e) {
            // Setter no disponible en aquesta versió; ignorem
            log.debug("Setter '{}' no disponible a JdbcMutableAclService", methodName);
        } catch (Exception e) {
            log.warn("No s'ha pogut establir '{}'", methodName, e);
        }
    }
}
