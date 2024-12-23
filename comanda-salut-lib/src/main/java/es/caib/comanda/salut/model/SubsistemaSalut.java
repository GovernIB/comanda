package es.caib.comanda.salut.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SubsistemaSalut extends EstatSalut {
    private final String codi;
}
