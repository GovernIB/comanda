package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IntegracioPeticions implements Serializable {
    private Long totalOk;
    private Long totalError;
    private Integer totalTempsMig;
    private Long peticionsOkUltimPeriode;
    private Long peticionsErrorUltimPeriode;
    private Integer tempsMigUltimPeriode;
    private String endpoint;
}
