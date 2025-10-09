package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubsistemaSalut extends EstatSalut {
    @NotNull @Size(min = 1)
    private String codi;

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
}
