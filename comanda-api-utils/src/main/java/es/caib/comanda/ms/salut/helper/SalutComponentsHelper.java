package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import es.caib.comanda.model.server.monitoring.IntegracioPeticions;
import es.caib.comanda.model.server.monitoring.IntegracioSalut;
import es.caib.comanda.model.server.monitoring.SubsistemaSalut;
import es.caib.comanda.ms.salut.helper.components.CalculSalutGlobal;
import es.caib.comanda.ms.salut.helper.components.EstadistiquesComponent;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMemoria;
import es.caib.comanda.ms.salut.helper.components.PoliticaSalutPerDefecte;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servei de salut per components dinàmics.
 *
 * - període=0 => retorna el darrer estat del component (com demanat)
 * - estat calculat => s'emmagatzema com a darrer estat
 */
public final class SalutComponentsHelper {

    private final MonitorComponentsMemoria monitor;
    private final PoliticaSalutPerDefecte politica;
    private final Function<String, Boolean> esCriticFunc;

    // darrer estat per component
    private final Map<String, EstatSalutEnum> darrerEstat = new HashMap<>();

    public SalutComponentsHelper(MonitorComponentsMemoria monitor,
                                 Function<String, Boolean> esCriticFunc) {
        this(monitor, null, esCriticFunc, Collections.emptyList());
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor,
                                 PoliticaSalutPerDefecte politica,
                                 Function<String, Boolean> esCriticFunc) {
        this(monitor, politica, esCriticFunc, Collections.emptyList());
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor,
                                 Function<String, Boolean> esCriticFunc,
                                 Collection<String> componentsInicials) {
        this(monitor, null, esCriticFunc, componentsInicials);
    }
    public SalutComponentsHelper(MonitorComponentsMemoria monitor,
                                 PoliticaSalutPerDefecte politica,
                                 Function<String, Boolean> esCriticFunc,
                                 Collection<String> componentsInicials) {
        this.monitor = Objects.requireNonNull(monitor);
        this.politica = politica == null ? new PoliticaSalutPerDefecte(monitor.getMidaFinestraFallback()) : politica;
        this.esCriticFunc = Objects.requireNonNull(esCriticFunc);
        if (componentsInicials != null) {
            componentsInicials.forEach(this.monitor::inicialitzaComponent);
        }
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor,
                                 Function<String, Boolean> esCriticFunc,
                                 Map<String, String> componentsInicialsAmbEndpoint) {
        this(monitor, null, esCriticFunc, componentsInicialsAmbEndpoint);
    }
    public SalutComponentsHelper(MonitorComponentsMemoria monitor,
                                 PoliticaSalutPerDefecte politica,
                                 Function<String, Boolean> esCriticFunc,
                                 Map<String, String> componentsInicialsAmbEndpoint) {
        this.monitor = Objects.requireNonNull(monitor);
        this.politica = politica == null ? new PoliticaSalutPerDefecte(monitor.getMidaFinestraFallback()) : politica;
        this.esCriticFunc = Objects.requireNonNull(esCriticFunc);
        if (componentsInicialsAmbEndpoint != null) {
            componentsInicialsAmbEndpoint.forEach(this.monitor::inicialitzaComponent);
        }
    }

    public void registraExit(String componentId, long duracioMs) {
        monitor.registraExit(componentId, duracioMs);
    }

    public void registraError(String componentId) {
        monitor.registraError(componentId);
    }

    /**
     * Genera l'informe:
     * - estadístiques per component (període i totals)
     * - estat per component
     * - estat global
     */
    public InformeSalutComponents obtenInforme() {
        Instant ara = Instant.now();

        Map<String, EstadistiquesComponent> estadistiques = monitor.obtenSnapshot();
        Map<String, EstatSalutEnum> estats = new HashMap<>();

        for (Map.Entry<String, EstadistiquesComponent> e : estadistiques.entrySet()) {
            String componentId = e.getKey();
            EstadistiquesComponent est = e.getValue();

            // període=0 => darrer estat
            if (est.getTotalPeriode() == 0) {
                estats.put(componentId, darrerEstat.getOrDefault(componentId, EstatSalutEnum.UNKNOWN));
                continue;
            }

            // fallback per component
            MonitorComponentsMemoria.DadesFallback fb = monitor.obtenFallback(componentId);
            EstatSalutEnum estatCalculat = politica.calculaEstat(est, fb.ok, fb.ko, fb.te);

            // guardam darrer estat
            darrerEstat.put(componentId, estatCalculat);

            estats.put(componentId, estatCalculat);
        }

        EstatSalutEnum estatGlobal = CalculSalutGlobal.calculaEstatGlobal(estats, esCriticFunc);

        return new InformeSalutComponents(ara, estadistiques, estats, estatGlobal);
    }

    public static final class InformeSalutComponents {
        private final Instant instantConsulta;
        private final Map<String, EstadistiquesComponent> estadistiques;
        private final Map<String, EstatSalutEnum> estats;
        private final EstatSalutEnum estatGlobal;

        public InformeSalutComponents(Instant instantConsulta,
                                      Map<String, EstadistiquesComponent> estadistiques,
                                      Map<String, EstatSalutEnum> estats,
                                      EstatSalutEnum estatGlobal) {
            this.instantConsulta = Objects.requireNonNull(instantConsulta);
            this.estadistiques = Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(estadistiques)));
            this.estats = Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(estats)));
            this.estatGlobal = Objects.requireNonNull(estatGlobal);
        }

        public Instant getInstantConsulta() { return instantConsulta; }
        public Map<String, EstadistiquesComponent> getEstadistiques() { return estadistiques; }
        public Map<String, EstatSalutEnum> getEstats() { return estats; }
        public EstatSalutEnum getEstatGlobal() { return estatGlobal; }

        // Si les dades que tenim al monitor son de integracions, podem convertir-les a l'estructura de salut
        public List<IntegracioSalut> toIntegracionsSalut() {
            return estats.entrySet().stream()
                    .map(e -> {
                        String id = e.getKey();
                        EstatSalutEnum estat = e.getValue();
                        EstadistiquesComponent stats = estadistiques.get(id);

                        return new IntegracioSalut()
                                .codi(id)
                                .estat(mapEstat(estat))
                                .peticions(mapPeticions(stats));
                    })
                    .collect(Collectors.toList());
        }

        // Si les dades que tenim al monitor son de subsistemes, podem convertir-les a l'estructura de salut
        public List<SubsistemaSalut> toSubsistemesSalut() {
            return estats.entrySet().stream()
                    .map(e -> {
                        String id = e.getKey();
                        EstatSalutEnum estat = e.getValue();
                        EstadistiquesComponent stats = estadistiques.get(id);

                        return new SubsistemaSalut()
                                .codi(id)
                                .estat(mapEstat(estat))
                                .totalOk(stats.getOkTotal())
                                .totalError(stats.getErrorTotal())
                                .totalTempsMig((int) Math.round(stats.getTempsMigMsTotal()))
                                .peticionsOkUltimPeriode(stats.getOkPeriode())
                                .peticionsErrorUltimPeriode(stats.getErrorPeriode())
                                .tempsMigUltimPeriode((int) Math.round(stats.getTempsMigMsPeriode()));
                    })
                    .collect(Collectors.toList());
        }

        private EstatSalutEnum mapEstat(EstatSalutEnum estat) {
            if (estat == null) return EstatSalutEnum.UNKNOWN;
            return estat;
        }

        private IntegracioPeticions mapPeticions(EstadistiquesComponent stats) {
            if (stats == null) return null;
            return new IntegracioPeticions()
                    .totalOk(stats.getOkTotal())
                    .totalError(stats.getErrorTotal())
                    .totalTempsMig((int) Math.round(stats.getTempsMigMsTotal()))
                    .peticionsOkUltimPeriode(stats.getOkPeriode())
                    .peticionsErrorUltimPeriode(stats.getErrorPeriode())
                    .tempsMigUltimPeriode((int) Math.round(stats.getTempsMigMsPeriode()))
                    .endpoint(stats.getEndpoint());
        }
    }
}
