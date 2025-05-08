package es.caib.comanda.estadistica.persist.repository.dialect;

import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.dialect.PostgreSQL10Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * Factoria per obtenir la implementació apropiada de FetRepositoryDialect
 * segons el dialecte de base de dades actual.
 */
@Component
public class FetRepositoryDialectFactory {

    @Autowired
    private OracleFetRepositoryDialect oracleFetRepositoryDialect;

    @Autowired
    private PostgreSQLFetRepositoryDialect postgreSQLFetRepositoryDialect;

    @Autowired
    private MariaDBFetRepositoryDialect mariaDBFetRepositoryDialect;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private FetRepositoryDialect currentDialect;

    @PostConstruct
    public void init() {
        Map<String, Object> hibernateProperties = entityManagerFactory.getProperties();

        String dialectClassName = (String) hibernateProperties.get("hibernate.dialect");

        if (dialectClassName == null) {
            // Default to Oracle if no dialect is specified
            currentDialect = oracleFetRepositoryDialect;
            return;
        }

        try {
            Class<?> dialectClass = Class.forName(dialectClassName);

            if (Oracle12cDialect.class.isAssignableFrom(dialectClass)) {
                currentDialect = oracleFetRepositoryDialect;
            } else if (PostgreSQL10Dialect.class.isAssignableFrom(dialectClass)) {
                currentDialect = postgreSQLFetRepositoryDialect;
            } else if (MariaDBDialect.class.isAssignableFrom(dialectClass)) {
                currentDialect = mariaDBFetRepositoryDialect;
            } else {
                // Default to Oracle for unknown dialects
                currentDialect = oracleFetRepositoryDialect;
            }
        } catch (ClassNotFoundException e) {
            // Default to Oracle if the dialect class cannot be found
            currentDialect = oracleFetRepositoryDialect;
        }
    }

    /**
     * Obté la implementació apropiada de FetRepositoryDialect pel dialecte de base de dades actual.
     *
     * @return La implementació de FetRepositoryDialect
     */
    public FetRepositoryDialect getDialect() {
        return currentDialect;
    }
}
