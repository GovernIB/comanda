package es.caib.comanda.estadistica.persist.repository.dialect;

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
    private EntityManagerFactory entityManagerFactory;

    private FetRepositoryDialect currentDialect;

    /**
     * Inicialitza el dialecte actual de FetRepositoryDialect en funció del dialecte de la base de dades configurat.
     * Aquesta inicialització es realitza en el moment de la construcció del component després de la injecció de dependències.
     *
     * Si no es defineix cap dialecte de Hibernate, per defecte s'assigna el dialecte per Oracle.
     * En cas que el dialecte configurat sigui per PostgreSQL, s'assigna el dialecte PostgreSQL corresponent.
     * Si el dialecte especificat no és reconegut o no es troba la classe del dialecte, també es selecciona per defecte Oracle.
     */
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
