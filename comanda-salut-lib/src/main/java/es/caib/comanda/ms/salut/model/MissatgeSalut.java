package es.caib.comanda.ms.salut.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class MissatgeSalut {
    private Date data;
    private String nivell;
    private String missatge;
}
