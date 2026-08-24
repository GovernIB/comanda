package es.caib.comanda.estadistica.persist.repository.dialect;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.time.Duration;

/**
 * Executa AbstractFetRepositoryDialectValuesTest contra un Oracle real (Testcontainers, imatge gvenzl/oracle-free),
 * amb l'esquema mínim de com_est_fet/com_est_temps.
 */
@Testcontainers(disabledWithoutDocker = true)
class OracleFetRepositoryDialectValuesTest extends AbstractFetRepositoryDialectValuesTest {

    @Container
    private static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*DATABASE IS READY TO USE!.*\\s")
                    .withTimes(1)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    private static NamedParameterJdbcTemplate jdbcTemplate;
    private static final OracleFetRepositoryDialect DIALECT = new OracleFetRepositoryDialect();

    @BeforeAll
    static void createSchema() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(oracle.jdbc.OracleDriver.class);
        dataSource.setUrl(ORACLE.getJdbcUrl());
        dataSource.setUsername(ORACLE.getUsername());
        dataSource.setPassword(ORACLE.getPassword());
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        // Oracle no té el tipus BIGINT (ORA-00902); NUMBER(19) és l'equivalent.
        jdbcTemplate.getJdbcTemplate().execute(
                "CREATE TABLE com_est_temps (" +
                        "id NUMBER(19) PRIMARY KEY, " +
                        "data DATE NOT NULL, " +
                        "anualitat NUMBER(4) NOT NULL, " +
                        "trimestre NUMBER(1) NOT NULL, " +
                        "mes NUMBER(2) NOT NULL, " +
                        "setmana NUMBER(2) NOT NULL, " +
                        "dia NUMBER(2) NOT NULL, " +
                        "dia_setmana VARCHAR2(2) NOT NULL)");
        jdbcTemplate.getJdbcTemplate().execute(
                "CREATE TABLE com_est_fet (" +
                        "id NUMBER(19) PRIMARY KEY, " +
                        "temps_id NUMBER(19) NOT NULL, " +
                        "dimensions_json VARCHAR2(4000), " +
                        "indicadors_json VARCHAR2(4000), " +
                        "entorn_app_id NUMBER(19) NOT NULL)");
    }

    @Override
    protected FetRepositoryDialect dialect() {
        return DIALECT;
    }

    @Override
    protected NamedParameterJdbcTemplate jdbc() {
        return jdbcTemplate;
    }
}
