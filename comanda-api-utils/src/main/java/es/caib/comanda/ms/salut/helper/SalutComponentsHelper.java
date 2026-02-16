package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import es.caib.comanda.ms.salut.helper.components.CalculSalutGlobal;
import es.caib.comanda.ms.salut.helper.components.EstadistiquesComponent;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMemoria;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMicrometer;
import es.caib.comanda.ms.salut.helper.components.PoliticaSalutPerDefecte;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Servei genèric per obtenir:
 * - estat per component (política + fallback per component)
 * - estat global (crítics/no crítics)
 *
 * IMPORTANT
 * - Si el període té 0 peticions, es retorna el darrer estat calculat.
 *
 * Període: des de la darrera consulta (reset-on-read del monitor).
 * Total: des de l'arrencada.
 */
public final class SalutComponentsHelper<K extends Enum<K>> {

    private final Class<K> tipusEnum;
    private final PoliticaSalutPerDefecte politica;
    private final Function<K, Boolean> esCriticFunc;

    // Un dels monitors (només un s'usarà)
    private final MonitorComponentsMemoria<K> monitorMemoria;
    private final MonitorComponentsMicrometer<K> monitorMicrometer;

    // Darrer estat calculat per subsistema (com el teu darrerEstat)
    private final Map<K, EstatSalutEnum> darrerEstat;

    public SalutComponentsHelper(Class<K> tipusEnum,
                                 MonitorComponentsMemoria<K> monitor,
                                 PoliticaSalutPerDefecte politica,
                                 Function<K, Boolean> esCriticFunc) {
        this.tipusEnum = Objects.requireNonNull(tipusEnum);
        this.monitorMemoria = Objects.requireNonNull(monitor);
        this.monitorMicrometer = null;
        this.politica = Objects.requireNonNull(politica);
        this.esCriticFunc = Objects.requireNonNull(esCriticFunc);

        // Inicialitzam darrerEstat a UNKNOWN per tots els subsistemes
        this.darrerEstat = new EnumMap<>(tipusEnum);
        for (K k : tipusEnum.getEnumConstants()) {
            this.darrerEstat.put(k, EstatSalutEnum.UNKNOWN);
        }
    }

    public SalutComponentsHelper(Class<K> tipusEnum,
                                 MonitorComponentsMicrometer<K> monitor,
                                 PoliticaSalutPerDefecte politica,
                                 Function<K, Boolean> esCriticFunc) {
        this.tipusEnum = Objects.requireNonNull(tipusEnum);
        this.monitorMemoria = null;
        this.monitorMicrometer = Objects.requireNonNull(monitor);
        this.politica = Objects.requireNonNull(politica);
        this.esCriticFunc = Objects.requireNonNull(esCriticFunc);

        // Inicialitzam darrerEstat a UNKNOWN per tots els subsistemes
        this.darrerEstat = new EnumMap<>(tipusEnum);
        for (K k : tipusEnum.getEnumConstants()) {
            this.darrerEstat.put(k, EstatSalutEnum.UNKNOWN);
        }
    }

    public void registraExit(K subsistema, long duracioMs) {
        if (monitorMemoria != null) monitorMemoria.registraExit(subsistema, duracioMs);
        else monitorMicrometer.registraExit(subsistema, duracioMs);
    }

    public void registraError(K subsistema) {
        if (monitorMemoria != null) monitorMemoria.registraError(subsistema);
        else monitorMicrometer.registraError(subsistema);
    }

    /**
     * Retorna:
     * - estadístiques del període i totals
     * - estat per subsistema
     * - estat global (crítics/no crítics)
     *
     * Regla clau:
     * - Si el període és 0 => retornam el darrer estat guardat.
     */
    public InformeSalut<K> obtenInforme() {
        Instant ara = Instant.now();

        Map<K, EstadistiquesComponent> estadistiques =
                (monitorMemoria != null) ? monitorMemoria.obtenSnapshot() : monitorMicrometer.obtenSnapshot();

        Map<K, EstatSalutEnum> estats = new EnumMap<>(tipusEnum);

        for (K k : tipusEnum.getEnumConstants()) {

            EstadistiquesComponent est = estadistiques.get(k);
            if (est == null) {
                // Si manca estadística, usam darrer estat (o UNKNOWN)
                EstatSalutEnum estat = darrerEstat.getOrDefault(k, EstatSalutEnum.UNKNOWN);
                estats.put(k, estat);
                continue;
            }

            // *** COMPORTAMENT DEMANAT: període = 0 => darrer estat ***
            if (est.getTotalPeriode() == 0) {
                EstatSalutEnum estat = darrerEstat.getOrDefault(k, EstatSalutEnum.UNKNOWN);
                estats.put(k, estat);
                continue;
            }

            // Obtenim fallback per subsistema (finestra de darreres N peticions)
            MonitorComponentsMemoria.DadesFallback fb =
                    (monitorMemoria != null) ? monitorMemoria.obtenFallback(k) : monitorMicrometer.obtenFallback(k);

            // Apliquem política (que usa fallback si hi ha poca mostra)
            EstatSalutEnum estatCalculat = politica.calculaEstat(est, fb.ok, fb.ko, fb.te);

            // Guardam darrer estat (com feies amb setDarrerEstat)
            darrerEstat.put(k, estatCalculat);

            estats.put(k, estatCalculat);
        }

        EstatSalutEnum global = CalculSalutGlobal.calculaEstatGlobal(estats, esCriticFunc);

        return new InformeSalut<>(ara, estadistiques, estats, global);
    }

    /**
     * Permet consultar el darrer estat guardat d'un subsistema (opcional).
     */
    public EstatSalutEnum obtenDarrerEstat(K subsistema) {
        return darrerEstat.getOrDefault(subsistema, EstatSalutEnum.UNKNOWN);
    }

    /**
     * DTO de retorn (informe complet).
     */
    public static final class InformeSalut<K extends Enum<K>> {
        private final Instant instantConsulta;
        private final Map<K, EstadistiquesComponent> estadistiques;
        private final Map<K, EstatSalutEnum> estats;
        private final EstatSalutEnum estatGlobal;

        public InformeSalut(Instant instantConsulta,
                            Map<K, EstadistiquesComponent> estadistiques,
                            Map<K, EstatSalutEnum> estats,
                            EstatSalutEnum estatGlobal) {
            this.instantConsulta = Objects.requireNonNull(instantConsulta);
            this.estadistiques = Objects.requireNonNull(estadistiques);
            this.estats = Objects.requireNonNull(estats);
            this.estatGlobal = Objects.requireNonNull(estatGlobal);
        }

        public Instant getInstantConsulta() { return instantConsulta; }
        public Map<K, EstadistiquesComponent> getEstadistiques() { return estadistiques; }
        public Map<K, EstatSalutEnum> getEstats() { return estats; }
        public EstatSalutEnum getEstatGlobal() { return estatGlobal; }
    }
}
