package es.caib.comanda.ms.generic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParellaCodiValor {
    @NotNull @Size(min = 1)
    private String codi;
    private String valor;
}
