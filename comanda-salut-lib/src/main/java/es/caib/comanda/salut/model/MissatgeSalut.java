package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.Builder;

import java.util.Date;

@Builder
@Getter
public class MissatgeSalut {
    private final Date data;
    private final String nivell;
    private final String missatge;
}
