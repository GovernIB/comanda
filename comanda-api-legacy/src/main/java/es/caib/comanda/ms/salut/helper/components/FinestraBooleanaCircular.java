package es.caib.comanda.ms.salut.helper.components;

final class FinestraBooleanaCircular {
    private final boolean[] buffer;
    private int mida = 0;
    private int index = 0;
    private long ok = 0;
    private long ko = 0;

    FinestraBooleanaCircular(int capacitat) {
        if (capacitat <= 0) {
            throw new IllegalArgumentException("capacitat ha de ser > 0");
        }
        this.buffer = new boolean[capacitat];
    }

    synchronized void afegeix(boolean esOk) {
        if (mida < buffer.length) {
            buffer[index] = esOk;
            mida++;
            index = (index + 1) % buffer.length;
            if (esOk) { ok++; } else { ko++; }
            return;
        }
        boolean anterior = buffer[index];
        if (anterior) { ok--; } else { ko--; }
        buffer[index] = esOk;
        if (esOk) { ok++; } else { ko++; }
        index = (index + 1) % buffer.length;
    }

    synchronized boolean esBuida() { return mida == 0; }
    synchronized long getOk() { return ok; }
    synchronized long getKo() { return ko; }
}
