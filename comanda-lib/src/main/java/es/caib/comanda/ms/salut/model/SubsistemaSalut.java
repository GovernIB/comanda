package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubsistemaSalut extends EstatSalut {
    private String codi;

    // Dades totals
    private Long totalOk;
    private Long totalError;
    private Integer totalTempsMig;

    // Dades per període consultat
    private Long peticionsOkUltimPeriode;
    private Long peticionsErrorUltimPeriode;
    private Integer tempsMigUltimPeriode;
}
