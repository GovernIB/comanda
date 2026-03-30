package es.caib.comanda.permisos.persist.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PermisosPersistenceConfigTest {

    @Test
    void getEntityPackages_retornaElsPaquetsDePersistenciaDelModul() {
        // Verifica que la configuració de persistència exposa els paquets d'entitats del mòdul permisos.
        PermisosPersistenceConfig config = new PermisosPersistenceConfig();

        String[] entityPackages = ReflectionTestUtils.invokeMethod(config, "getEntityPackages");

        assertThat(entityPackages).containsExactly("es.caib.comanda.ms.persist", "es.caib.comanda.permisos.persist");
    }

    @Test
    void enableJpaRepositories_apuntaAlPaquetDeRepositorisDelModul() {
        // Comprova que l'anotació JPA inclou el paquet de repositoris del mòdul permisos.
        EnableJpaRepositories enableJpaRepositories = PermisosPersistenceConfig.class.getAnnotation(EnableJpaRepositories.class);

        assertThat(enableJpaRepositories.basePackages()).contains("es.caib.comanda.permisos.persist");
    }
}
