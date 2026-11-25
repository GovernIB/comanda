package es.caib.comanda.monitor.logic.config;

import com.zaxxer.hikari.HikariDataSource;
import es.caib.comanda.monitor.logic.helper.HikariPoolStatsTrackerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * {@link BeanPostProcessor} que registra {@link HikariPoolStatsTrackerFactory} en tots els beans
 * {@link HikariDataSource} del context perquè el monitor de rendiment pugui obtenir {@code maxWaitTimeMs}
 * sense modificar la configuració genèrica de persistència ({@code BasePersistenceConfig}).
 */
@Component
@Slf4j
public class HikariPoolStatsPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof DataSource)) {
            return bean;
        }
        HikariDataSource hikari = unwrapHikari((DataSource) bean);
        if (hikari != null) {
            instrumentHikari(hikari, beanName);
        }
        return bean;
    }

    private void instrumentHikari(HikariDataSource hikari, String beanName) {
        try {
            if (hikari.isClosed()
                    || hikari.getMetricRegistry() != null
                    || hikari.getMetricsTrackerFactory() instanceof HikariPoolStatsTrackerFactory) {
                return;
            }
            // Si el pool ja s'ha iniciat i ja té una altra MetricsTrackerFactory fixada,
            // HikariCP no permet substituir-la (llançaria IllegalStateException).
            if (hikari.getHikariPoolMXBean() != null && hikari.getMetricsTrackerFactory() != null) {
                log.debug("S'omet HikariPoolStatsTrackerFactory per al bean '{}': el pool ja està iniciat amb una altra MetricsTrackerFactory", beanName);
                return;
            }
            hikari.setMetricsTrackerFactory(new HikariPoolStatsTrackerFactory(
                    hikari.getMetricsTrackerFactory(),
                    hikari::getConnectionTimeout
            ));
        } catch (Exception e) {
            log.debug("No s'ha pogut registrar HikariPoolStatsTrackerFactory al DataSource '{}': {}", beanName, e.getMessage());
        }
    }

    private HikariDataSource unwrapHikari(DataSource ds) {
        try {
            if (ds instanceof HikariDataSource) {
                return (HikariDataSource) ds;
            }
            if (ds.isWrapperFor(HikariDataSource.class)) {
                return ds.unwrap(HikariDataSource.class);
            }
        } catch (Exception e) {
            log.debug("No s'ha pogut desembolcallar DataSource com a HikariDataSource: {}", e.getMessage());
        }
        return null;
    }
}
