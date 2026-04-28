package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepositoryImpl;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.model.v1.salut.SalutNivell;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:salut-testdb;DB_CLOSE_DELAY=-1;MODE=Oracle",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.liquibase.enabled=false"
})
@ContextConfiguration(classes = SalutRepositoryIT.TestRepositoryConfig.class)
class SalutRepositoryIT {

    @Autowired
    private SalutRepository salutRepository;

    @Autowired
    private SalutMissatgeRepository salutMissatgeRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void informeSalutLast_quanElDarrerRegistreAnteriorNoEsDeTipusMinut_exclouLEntornDelResultat() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 3, 16, 8, 0);
        SalutEntity oldEntorn1 = persistSalut(10L, baseTime.minusMinutes(5), TipusRegistreSalut.MINUT, SalutEstat.DOWN);
        SalutEntity latestEntorn1 = persistSalut(10L, baseTime.minusMinutes(1), TipusRegistreSalut.MINUT, SalutEstat.UP);
        persistSalut(10L, baseTime.plusMinutes(1), TipusRegistreSalut.MINUT, SalutEstat.ERROR);
        persistSalut(20L, baseTime.minusMinutes(2), TipusRegistreSalut.MINUTS, SalutEstat.WARN);
        SalutEntity latestEntorn2 = persistSalut(20L, baseTime.minusMinutes(3), TipusRegistreSalut.MINUT, SalutEstat.WARN);
        entityManager.flush();
        entityManager.clear();

        List<SalutEntity> result = salutRepository.informeSalutLast(List.of(10L, 20L), baseTime);

        assertThat(result)
                .extracting(SalutEntity::getId)
                .containsExactly(latestEntorn1.getId())
                .doesNotContain(oldEntorn1.getId(), latestEntorn2.getId());
    }

    @Test
    void findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById_quanHiHaMissatges_formulesIOrdenacioFuncionen() {
        SalutEntity first = persistSalut(30L, LocalDateTime.of(2026, 3, 16, 7, 55), TipusRegistreSalut.MINUT, SalutEstat.UP);
        SalutEntity second = persistSalut(30L, LocalDateTime.of(2026, 3, 16, 7, 58), TipusRegistreSalut.MINUT, SalutEstat.WARN);
        persistMissatge(second, SalutNivell.WARN, "warn");
        entityManager.flush();
        entityManager.clear();

        List<SalutEntity> result = salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                30L,
                LocalDateTime.of(2026, 3, 16, 7, 50),
                TipusRegistreSalut.MINUT);

        assertThat(result)
                .extracting(SalutEntity::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(result.get(1).getMissatgeWarnCount()).isEqualTo(1);
    }

    private SalutEntity persistSalut(Long entornAppId, LocalDateTime data, TipusRegistreSalut tipus, SalutEstat estat) {
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(entornAppId);
        entity.setData(data);
        entity.setDataApp(data);
        entity.setTipusRegistre(tipus);
        entity.setAppEstat(estat);
        entity.setBdEstat(estat);
        entity.setAppLatencia(25);
        entity.setBdLatencia(10);
        entity.setNumElements(1);
        entity.updateAppCountByEstat(estat);
        entity.updateBdCountByEstat(estat);
        return salutRepository.saveAndFlush(entity);
    }

    private void persistMissatge(SalutEntity salut, SalutNivell nivell, String missatge) {
        SalutMissatgeEntity entity = new SalutMissatgeEntity();
        entity.setSalut(salut);
        entity.setData(salut.getData());
        entity.setNivell(nivell);
        entity.setMissatge(missatge);
        salutMissatgeRepository.saveAndFlush(entity);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackages = {
            "es.caib.comanda.ms.persist",
            "es.caib.comanda.salut.persist"
    })
    @EnableJpaRepositories(
            basePackages = {
                    "es.caib.comanda.ms.persist",
                    "es.caib.comanda.salut.persist"
            },
            repositoryBaseClass = BaseRepositoryImpl.class
    )
    static class TestRepositoryConfig {
    }
}
