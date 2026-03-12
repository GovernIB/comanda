package es.caib.comanda.acl.persist.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class AclPersistenceConfigTest {

    @Test
    void getEntityPackages_retornaElsPaquetsDePersistenciaAcl() {
        // Comprova que la configuració de persistència declara els paquets d'entitats esperats.
        AclPersistenceConfig config = new AclPersistenceConfig();

        String[] entityPackages = ReflectionTestUtils.invokeMethod(config, "getEntityPackages");

        assertThat(entityPackages).containsExactly("es.caib.comanda.ms.persist", "es.caib.comanda.acl.persist");
    }

    @Test
    void anotacioEnableJpaRepositories_inclouElPaquetDeRepositorisAcl() {
        // Verifica que l'anotació JPA apunta al paquet de repositoris del mòdul ACL.
        EnableJpaRepositories repositories = AclPersistenceConfig.class.getAnnotation(EnableJpaRepositories.class);

        assertThat(repositories.basePackages()).contains("es.caib.comanda.acl.persist");
    }
}
