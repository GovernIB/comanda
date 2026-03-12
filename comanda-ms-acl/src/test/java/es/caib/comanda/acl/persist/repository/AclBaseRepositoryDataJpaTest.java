package es.caib.comanda.acl.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.ms.persist.repository.BaseRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.liquibase.enabled=false"
})
@ContextConfiguration(classes = AclBaseRepositoryDataJpaTest.TestRepositoryConfig.class)
class AclBaseRepositoryDataJpaTest {

    @Autowired
    private TestAclRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void saveAndFlush_quanEsPersisteixEntitat_assignaUnIdentificador() {
        // Comprova que el repositori base heretat persisteix l'entitat i li assigna id.
        TestAclRecord entity = new TestAclRecord();
        entity.setNom("alpha");

        TestAclRecord saved = repository.saveAndFlush(entity);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void refresh_quanLaFilaCanviaABD_sincronitzaLestatDeLentitat() {
        // Verifica que el mètode heretat refresh torna a carregar l'estat actual de base de dades.
        TestAclRecord entity = repository.saveAndFlush(new TestAclRecord(null, "beta"));
        testEntityManager.getEntityManager()
                .createNativeQuery("update acl_test_record set nom = 'beta-actualitzat' where id = ?")
                .setParameter(1, entity.getId())
                .executeUpdate();

        repository.refresh(entity);

        assertThat(entity.getNom()).isEqualTo("beta-actualitzat");
    }

    @Test
    void detach_quanEsDesvinculaEntitat_laSessioDeixaDeGestionarLaInstancia() {
        // Comprova que el mètode heretat detach desenganxa l'entitat del context de persistència.
        TestAclRecord entity = repository.saveAndFlush(new TestAclRecord(null, "gamma"));

        repository.detach(entity);

        assertThat(testEntityManager.getEntityManager().contains(entity)).isFalse();
    }

    @Test
    void merge_quanLEntitatEstaDetached_persistixElsCanvisSobreUnaNovaInstanciaGestionada() {
        // Verifica que el mètode heretat merge reaplica els canvis d'una entitat separada del context.
        TestAclRecord entity = repository.saveAndFlush(new TestAclRecord(null, "delta"));
        repository.detach(entity);
        entity.setNom("delta-merge");

        TestAclRecord merged = repository.merge(entity);
        repository.flush();
        testEntityManager.clear();

        TestAclRecord reloaded = repository.findById(merged.getId()).orElseThrow();
        assertThat(reloaded.getNom()).isEqualTo("delta-merge");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = AclBaseRepositoryDataJpaTest.TestAclRecord.class)
    @EnableJpaRepositories(
            considerNestedRepositories = true,
            basePackageClasses = AclBaseRepositoryDataJpaTest.class,
            repositoryBaseClass = BaseRepositoryImpl.class
    )
    static class TestRepositoryConfig {
    }

    interface TestAclRepository extends BaseRepository<TestAclRecord, Long> {
    }

    @Entity
    @Table(name = "acl_test_record")
    static class TestAclRecord {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String nom;

        TestAclRecord() {
        }

        TestAclRecord(Long id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }
    }
}
