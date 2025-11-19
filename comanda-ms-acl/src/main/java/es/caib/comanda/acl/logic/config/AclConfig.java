package es.caib.comanda.acl.logic.config;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.config.BaseAclConfig;
import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració de les ACLs de Spring Security.
 * 
 * @author Límit Tecnologies
 */
@Configuration
public class AclConfig extends BaseAclConfig {

	@Override
	protected String getAclCacheName() {
		return HazelCastCacheConfig.ACL_CACHE;
	}

	@Override
	public String getDbTablePrefix() {
		return BaseConfig.DB_PREFIX;
	}

}
