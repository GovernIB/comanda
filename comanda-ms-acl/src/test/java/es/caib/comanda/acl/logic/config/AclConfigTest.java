package es.caib.comanda.acl.logic.config;

import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AclConfigTest {

    @Test
    void getAclCacheName_retornaElNomDeCacheCompartitPerAcl() {
        // Verifica que la configuració lògica ACL usa el nom de cache compartit esperat.
        AclConfig config = new AclConfig();

        String aclCacheName = ReflectionTestUtils.invokeMethod(config, "getAclCacheName");

        assertThat(aclCacheName).isEqualTo(HazelCastCacheConfig.ACL_CACHE);
    }
}
