package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracioPeticions {
    private long totalOk;
    private long totalError;
    @Builder.Default
    private Map<String, Long> organOk = new HashMap<>();
    @Builder.Default
    private Map<String, Long> organError = new HashMap<>();

    @Synchronized
    public void addPeticioTotal(String organ) {
        totalOk++;
        if (organ != null && !organ.isBlank()) {
            organOk.put(organ, organOk.getOrDefault(organ, 0L) + 1);
        }
    }

    @Synchronized
    public void addPeticioError(String organ) {
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
