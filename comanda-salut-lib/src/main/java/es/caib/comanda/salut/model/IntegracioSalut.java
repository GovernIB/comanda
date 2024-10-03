package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class IntegracioSalut extends EstatSalut {
    private final String codi;
    private final IntegracioPeticions peticions;
}
