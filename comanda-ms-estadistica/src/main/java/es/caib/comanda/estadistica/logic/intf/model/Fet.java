package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class Fet extends BaseResource<Long> {

    @NotNull
    private Indicador indicator;
    @NotNull
    private Temps temps;
    @NotEmpty
    private Set<DimensioValor> dimensioValors;
    @NotNull
    private Double valor;

}
