package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Synchronized;

import java.util.Map;

@Getter
@NoArgsConstructor
public class IntegracioPeticions {
    private long totalOk;
    private long totalError;
    private Map<String, Long> organOk;
    private Map<String, Long> organError;

    @Synchronized
    public void updatePeticioTotal(String organ) {
        totalOk++;
        if (organ != null && !organ.isBlank()) {
            organOk.put(organ, organOk.getOrDefault(organ, 0L) + 1);
        }
    }

    @Synchronized
    public void updatePeticioError(String organ) {
        totalError++;
        if (organ != null && !organ.isBlank()) {
            organError.put(organ, organError.getOrDefault(organ, 0L) + 1);
        }
    }

    @Synchronized
    public void reset() {
        totalOk = 0;
        totalError = 0;
        organOk.clear();
        organError.clear();
    }
}
