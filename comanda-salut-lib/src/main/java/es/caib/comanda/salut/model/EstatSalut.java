package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EstatSalut {
    private EstatSalutEnum estat;
    private Integer latencia;
}
