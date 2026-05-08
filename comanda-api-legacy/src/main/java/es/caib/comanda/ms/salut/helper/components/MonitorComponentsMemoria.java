package es.caib.comanda.ms.salut.helper.components;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class MonitorComponentsMemoria {
    private final int midaFinestraFallback;
    private final ConcurrentHashMap<String, Entrada> entrades = new ConcurrentHashMap<String, Entrada>();

    public MonitorComponentsMemoria(int midaFinestraFallback) {
        if (midaFinestraFallback <= 0) {
            throw new IllegalArgumentException("midaFinestraFallback ha de ser > 0");
        }
        this.midaFinestraFallback = midaFinestraFallback;
    }

    public int getMidaFinestraFallback() { return midaFinestraFallback; }

    public void registraExit(String componentId, long duracioMs) {
        if (isBlank(componentId)) { return; }
        if (duracioMs < 0) { duracioMs = 0; }
        Entrada entrada = getOrCreateEntrada(componentId);
        entrada.okTotal.incrementAndGet();
        entrada.sumaLatenciaMsTotal.addAndGet(duracioMs);
        entrada.okPeriode.incrementAndGet();
        entrada.sumaLatenciaMsPeriode.addAndGet(duracioMs);
        entrada.finestra.afegeix(true);
    }

    public void registraError(String componentId) {
        if (isBlank(componentId)) { return; }
        Entrada entrada = getOrCreateEntrada(componentId);
        entrada.errorTotal.incrementAndGet();
        entrada.errorPeriode.incrementAndGet();
        entrada.finestra.afegeix(false);
    }

    public void inicialitzaComponent(String componentId) {
        inicialitzaComponent(componentId, null);
    }

    public void inicialitzaComponent(String componentId, String endpoint) {
        if (isBlank(componentId)) { return; }
        Entrada entrada = getOrCreateEntrada(componentId);
        if (endpoint != null) { entrada.endpoint = endpoint; }
    }

    public Map<String, EstadistiquesComponent> obtenSnapshot() {
        Date ara = new Date();
        Map<String, EstadistiquesComponent> out = new HashMap<String, EstadistiquesComponent>();
        for (Map.Entry<String, Entrada> entry : entrades.entrySet()) {
            out.put(entry.getKey(), entry.getValue().creaSnapshot(entry.getKey(), ara));
        }
        for (Entrada entrada : entrades.values()) {
            entrada.resetejaPeriode();
        }
        return Collections.unmodifiableMap(out);
    }

    public DadesFallback obtenFallback(String componentId) {
        Entrada entrada = entrades.get(componentId);
        if (entrada == null) {
            return new DadesFallback(0, 0, false, null);
        }
        boolean te = !entrada.finestra.esBuida();
        return new DadesFallback(entrada.finestra.getOk(), entrada.finestra.getKo(), te, entrada.endpoint);
    }

    private Entrada getOrCreateEntrada(String componentId) {
        Entrada existent = entrades.get(componentId);
        if (existent != null) { return existent; }
        Entrada creada = new Entrada(midaFinestraFallback);
        Entrada anterior = entrades.putIfAbsent(componentId, creada);
        return anterior != null ? anterior : creada;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static final class DadesFallback {
        public final long ok;
        public final long ko;
        public final boolean te;
        public final String endpoint;

        public DadesFallback(long ok, long ko, boolean te, String endpoint) {
            this.ok = ok;
            this.ko = ko;
            this.te = te;
            this.endpoint = endpoint;
        }
    }

    private static final class Entrada {
        private volatile String endpoint;
        private final AtomicLong okTotal = new AtomicLong();
        private final AtomicLong errorTotal = new AtomicLong();
        private final AtomicLong sumaLatenciaMsTotal = new AtomicLong();
        private final AtomicLong okPeriode = new AtomicLong();
        private final AtomicLong errorPeriode = new AtomicLong();
        private final AtomicLong sumaLatenciaMsPeriode = new AtomicLong();
        private final FinestraBooleanaCircular finestra;

        private Entrada(int midaFinestraFallback) {
            this.finestra = new FinestraBooleanaCircular(midaFinestraFallback);
        }

        private EstadistiquesComponent creaSnapshot(String componentId, Date ara) {
            long okP = okPeriode.get();
            long koP = errorPeriode.get();
            double migP = okP > 0 ? (sumaLatenciaMsPeriode.get() * 1.0d) / okP : 0.0d;
            long okT = okTotal.get();
            long koT = errorTotal.get();
            double migT = okT > 0 ? (sumaLatenciaMsTotal.get() * 1.0d) / okT : 0.0d;
            return new EstadistiquesComponent(componentId, endpoint, okP, koP, migP, okT, koT, migT, ara);
        }

        private void resetejaPeriode() {
            okPeriode.set(0);
            errorPeriode.set(0);
            sumaLatenciaMsPeriode.set(0);
        }
    }
}
