package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracioSalut extends EstatSalut {
    @NotNull @Size(min = 1)
    private String codi;
    @Valid
    private IntegracioPeticions peticions;
}
