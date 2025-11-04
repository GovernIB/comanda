package es.caib.comanda.acl.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

/**
 * Configuració de Spring Security ACL com a motor intern.
 */
@Configuration
@Profile("!back")
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
        // Es poden personalitzar noms de taules si s'usen prefixos diferents dels estàndard
        // service.setClassIdentityQuery("select id, class from com_acl_class where id = ?"); // exemple
        return service;
    }
}
