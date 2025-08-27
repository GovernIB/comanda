package es.caib.comanda.ms.generic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParellaCodiValor {
    private String codi;
    private String valor;
}
