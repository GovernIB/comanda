package es.caib.comanda.estadistica.persist.repository.dialect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Executa AbstractFetRepositoryDialectValuesTest contra un PostgreSQL real (Testcontainers), amb l'esquema mínim
 * de com_est_fet/com_est_temps (idèntic al de comanda-ms-estadistica/liquibase, incloent el tipus varchar de
 * dimensions_json/indicadors_json - no jsonb - que és precisament el que exigeix el cast explícit dins del
 * dialecte).
 */
@Testcontainers(disabledWithoutDocker = true)
class PostgreSQLFetRepositoryDialectValuesTest extends AbstractFetRepositoryDialectValuesTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static HikariDataSource dataSource;
    private static NamedParameterJdbcTemplate jdbcTemplate;
    private static final PostgreSQLFetRepositoryDialect DIALECT = new PostgreSQLFetRepositoryDialect();

    @BeforeAll
    static void createSchema() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(POSTGRES.getJdbcUrl());
        config.setUsername(POSTGRES.getUsername());
        config.setPassword(POSTGRES.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        dataSource = new HikariDataSource(config);
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        jdbcTemplate.getJdbcTemplate().execute(
                "CREATE TABLE com_est_temps (" +
                        "id BIGINT PRIMARY KEY, " +
                        "data DATE NOT NULL, " +
                        "anualitat INT NOT NULL, " +
                        "trimestre INT NOT NULL, " +
                        "mes INT NOT NULL, " +
                        "setmana INT NOT NULL, " +
                        "dia INT NOT NULL, " +
                        "dia_setmana VARCHAR(2) NOT NULL)");
        jdbcTemplate.getJdbcTemplate().execute(
                "CREATE TABLE com_est_fet (" +
                        "id BIGINT PRIMARY KEY, " +
                        "temps_id BIGINT NOT NULL, " +
                        "dimensions_json VARCHAR(4000), " +
                        "indicadors_json VARCHAR(4000), " +
                        "entorn_app_id BIGINT NOT NULL)");
    }

    @AfterAll
    static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
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
