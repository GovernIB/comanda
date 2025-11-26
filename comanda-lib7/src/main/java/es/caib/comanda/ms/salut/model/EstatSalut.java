package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstatSalut {
    @Valid
    private EstatSalutEnum estat;
    private Integer latencia;
}
