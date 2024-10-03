package es.caib.comanda.salut.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DetallSalut {
    private final String codi;
    private final String nom;
    private final String valor;
}
