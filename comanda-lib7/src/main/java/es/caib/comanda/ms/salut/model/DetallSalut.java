package es.caib.comanda.ms.salut.model;

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
public class DetallSalut {
    @NotNull @Size(min = 1)
    private String codi;
    @NotNull @Size(min = 1)
    private String nom;
    @NotNull @Size(min = 1)
    private String valor;
}
