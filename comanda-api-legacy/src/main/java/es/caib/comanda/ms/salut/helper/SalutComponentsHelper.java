package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.ms.salut.helper.components.CalculSalutGlobal;
import es.caib.comanda.ms.salut.helper.components.EstadistiquesComponent;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMemoria;
import es.caib.comanda.ms.salut.helper.components.PoliticaSalutPerDefecte;
import es.caib.comanda.ms.salut.model.EstatSalutEnum;
import es.caib.comanda.ms.salut.model.IntegracioPeticions;
import es.caib.comanda.ms.salut.model.IntegracioSalut;
import es.caib.comanda.ms.salut.model.SubsistemaSalut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SalutComponentsHelper {

    public interface CriticitatComponentResolver extends CalculSalutGlobal.CriticitatComponentResolver {
    }

    private final MonitorComponentsMemoria monitor;
    private final PoliticaSalutPerDefecte politica;
    private final CriticitatComponentResolver esCriticFunc;
    private final Map<String, EstatSalutEnum> darrerEstat = new HashMap<String, EstatSalutEnum>();

    public SalutComponentsHelper(MonitorComponentsMemoria monitor, CriticitatComponentResolver esCriticFunc) {
        this(monitor, null, esCriticFunc, (Collection<String>) null);
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor, PoliticaSalutPerDefecte politica, CriticitatComponentResolver esCriticFunc) {
        this(monitor, politica, esCriticFunc, (Collection<String>) null);
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor, CriticitatComponentResolver esCriticFunc, Collection<String> componentsInicials) {
        this(monitor, null, esCriticFunc, componentsInicials);
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor, PoliticaSalutPerDefecte politica, CriticitatComponentResolver esCriticFunc, Collection<String> componentsInicials) {
        if (monitor == null) {
            throw new IllegalArgumentException("monitor no pot ser null");
        }
        if (esCriticFunc == null) {
            throw new IllegalArgumentException("esCriticFunc no pot ser null");
        }
        this.monitor = monitor;
        this.politica = politica != null ? politica : new PoliticaSalutPerDefecte(monitor.getMidaFinestraFallback());
        this.esCriticFunc = esCriticFunc;
        if (componentsInicials != null) {
            for (String componentId : componentsInicials) {
                this.monitor.inicialitzaComponent(componentId);
            }
        }
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor, CriticitatComponentResolver esCriticFunc, Map<String, String> componentsInicialsAmbEndpoint) {
        this(monitor, null, esCriticFunc, componentsInicialsAmbEndpoint);
    }

    public SalutComponentsHelper(MonitorComponentsMemoria monitor, PoliticaSalutPerDefecte politica, CriticitatComponentResolver esCriticFunc, Map<String, String> componentsInicialsAmbEndpoint) {
        if (monitor == null) {
            throw new IllegalArgumentException("monitor no pot ser null");
        }
        if (esCriticFunc == null) {
            throw new IllegalArgumentException("esCriticFunc no pot ser null");
        }
        this.monitor = monitor;
        this.politica = politica != null ? politica : new PoliticaSalutPerDefecte(monitor.getMidaFinestraFallback());
        this.esCriticFunc = esCriticFunc;
        if (componentsInicialsAmbEndpoint != null) {
            for (Map.Entry<String, String> entry : componentsInicialsAmbEndpoint.entrySet()) {
                this.monitor.inicialitzaComponent(entry.getKey(), entry.getValue());
            }
        }
    }

    public void registraExit(String componentId, long duracioMs) {
        monitor.registraExit(componentId, duracioMs);
    }

    public void registraError(String componentId) {
        monitor.registraError(componentId);
    }

    public InformeSalutComponents obtenInforme() {
        Date ara = new Date();
        Map<String, EstadistiquesComponent> estadistiques = monitor.obtenSnapshot();
        Map<String, EstatSalutEnum> estats = new HashMap<String, EstatSalutEnum>();
        for (Map.Entry<String, EstadistiquesComponent> entry : estadistiques.entrySet()) {
            String componentId = entry.getKey();
            EstadistiquesComponent est = entry.getValue();
            if (est.getTotalPeriode() == 0) {
                EstatSalutEnum estatAnterior = darrerEstat.get(componentId);
                estats.put(componentId, estatAnterior != null ? estatAnterior : EstatSalutEnum.UNKNOWN);
                continue;
            }
            MonitorComponentsMemoria.DadesFallback fb = monitor.obtenFallback(componentId);
            EstatSalutEnum estatCalculat = politica.calculaEstat(est, fb.ok, fb.ko, fb.te);
            darrerEstat.put(componentId, estatCalculat);
            estats.put(componentId, estatCalculat);
        }
        EstatSalutEnum estatGlobal = CalculSalutGlobal.calculaEstatGlobal(estats, esCriticFunc);
        return new InformeSalutComponents(ara, estadistiques, estats, estatGlobal);
    }

    public static final class InformeSalutComponents {
        private final Date instantConsulta;
        private final Map<String, EstadistiquesComponent> estadistiques;
        private final Map<String, EstatSalutEnum> estats;
        private final EstatSalutEnum estatGlobal;

        public InformeSalutComponents(Date instantConsulta, Map<String, EstadistiquesComponent> estadistiques, Map<String, EstatSalutEnum> estats, EstatSalutEnum estatGlobal) {
            if (instantConsulta == null || estadistiques == null || estats == null || estatGlobal == null) {
                throw new IllegalArgumentException("cap paràmetre pot ser null");
            }
            this.instantConsulta = instantConsulta;
            this.estadistiques = Collections.unmodifiableMap(new HashMap<String, EstadistiquesComponent>(estadistiques));
            this.estats = Collections.unmodifiableMap(new HashMap<String, EstatSalutEnum>(estats));
            this.estatGlobal = estatGlobal;
        }

        public Date getInstantConsulta() { return instantConsulta; }
        public Map<String, EstadistiquesComponent> getEstadistiques() { return estadistiques; }
        public Map<String, EstatSalutEnum> getEstats() { return estats; }
        public EstatSalutEnum getEstatGlobal() { return estatGlobal; }

        public List<IntegracioSalut> toIntegracionsSalut() {
            List<IntegracioSalut> result = new ArrayList<IntegracioSalut>();
            for (Map.Entry<String, EstatSalutEnum> entry : estats.entrySet()) {
                String id = entry.getKey();
                EstatSalutEnum estat = mapEstat(entry.getValue());
                EstadistiquesComponent stats = estadistiques.get(id);
                result.add(IntegracioSalut.builder().codi(id).estat(estat).peticions(mapPeticions(stats)).build());
            }
            return result;
        }

        public List<SubsistemaSalut> toSubsistemesSalut() {
            List<SubsistemaSalut> result = new ArrayList<SubsistemaSalut>();
            for (Map.Entry<String, EstatSalutEnum> entry : estats.entrySet()) {
                String id = entry.getKey();
                EstatSalutEnum estat = mapEstat(entry.getValue());
                EstadistiquesComponent stats = estadistiques.get(id);
                result.add(SubsistemaSalut.builder()
                        .codi(id)
                        .estat(estat)
                        .totalOk(stats != null ? Long.valueOf(stats.getOkTotal()) : Long.valueOf(0L))
                        .totalError(stats != null ? Long.valueOf(stats.getErrorTotal()) : Long.valueOf(0L))
                        .totalTempsMig(Integer.valueOf(stats != null ? (int) Math.round(stats.getTempsMigMsTotal()) : 0))
                        .peticionsOkUltimPeriode(stats != null ? Long.valueOf(stats.getOkPeriode()) : Long.valueOf(0L))
                        .peticionsErrorUltimPeriode(stats != null ? Long.valueOf(stats.getErrorPeriode()) : Long.valueOf(0L))
                        .tempsMigUltimPeriode(Integer.valueOf(stats != null ? (int) Math.round(stats.getTempsMigMsPeriode()) : 0))
                        .build());
            }
            return result;
        }

        private EstatSalutEnum mapEstat(EstatSalutEnum estat) {
            return estat != null ? estat : EstatSalutEnum.UNKNOWN;
        }

        private IntegracioPeticions mapPeticions(EstadistiquesComponent stats) {
            if (stats == null) {
                return null;
            }
            return IntegracioPeticions.builder()
                    .totalOk(Long.valueOf(stats.getOkTotal()))
                    .totalError(Long.valueOf(stats.getErrorTotal()))
                    .totalTempsMig(Integer.valueOf((int) Math.round(stats.getTempsMigMsTotal())))
                    .peticionsOkUltimPeriode(Long.valueOf(stats.getOkPeriode()))
                    .peticionsErrorUltimPeriode(Long.valueOf(stats.getErrorPeriode()))
                    .tempsMigUltimPeriode(Integer.valueOf((int) Math.round(stats.getTempsMigMsPeriode())))
                    .endpoint(stats.getEndpoint())
                    .build();
        }
    }
}
