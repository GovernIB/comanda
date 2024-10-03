package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class EstatSalut {
    private final EstatSalutEnum estat;
    private final Long latencia;
}
