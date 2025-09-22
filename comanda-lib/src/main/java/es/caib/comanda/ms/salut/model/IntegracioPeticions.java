package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracioPeticions {

    // Dades totals
    private long totalOk;
    private long totalError;
    private Integer totalTempsMig;

    // Dades per període consultat
    private Long peticionsOkUltimPeriode;
    private Long peticionsErrorUltimPeriode;
    private Integer tempsMigUltimPeriode;

    private String endpoint;

    @Builder.Default
    private Map<String, IntegracioPeticions> peticionsPerEntorn = new HashMap<>();

}
