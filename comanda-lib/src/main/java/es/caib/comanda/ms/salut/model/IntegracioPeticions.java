package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracioPeticions {

    // Dades totals
    @NotNull
    private Long totalOk;
    @NotNull
    private Long totalError;
    @NotNull
    private Integer totalTempsMig;

    // Dades per període consultat
    @NotNull
    private Long peticionsOkUltimPeriode;
    @NotNull
    private Long peticionsErrorUltimPeriode;
    @NotNull
    private Integer tempsMigUltimPeriode;

    @Size(max = 255)
    private String endpoint;

    @Builder.Default
    private Map<String, IntegracioPeticions> peticionsPerEntorn = new HashMap<>();

}
