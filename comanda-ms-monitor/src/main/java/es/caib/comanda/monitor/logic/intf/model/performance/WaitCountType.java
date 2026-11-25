package es.caib.comanda.monitor.logic.intf.model.performance;

/**
 * Indica com s'obté el comptador d'esperes ({@code waitCount}) segons el proveïdor de pool.
 */
public enum WaitCountType {
    /**
     * Delta acumulat durant el darrer interval de mostreig (JBoss IronJacamar {@code WaitCount}).
     */
    INTERVAL_DELTA,
    /**
     * Fils actualment en espera en l'instant del mostreig (HikariCP {@code threadsAwaitingConnection}).
     */
    INSTANTANEOUS
}
