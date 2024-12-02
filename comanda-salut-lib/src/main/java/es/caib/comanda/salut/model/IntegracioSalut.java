package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
public class IntegracioSalut extends EstatSalut {
    private String codi;
    private IntegracioPeticions peticions;
}
