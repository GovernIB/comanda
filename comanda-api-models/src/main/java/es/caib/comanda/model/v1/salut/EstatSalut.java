package es.caib.comanda.model.v1.salut;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstatSalut {
    @Valid
    private EstatSalutEnum estat;
    private Integer latencia;
}
