package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class DimensioValor extends BaseResource<Long> {

    @NotNull
    @Size(max = 255)
    private String valor;
    @NotNull
    private ResourceReference<Dimensio, Long> dimensio;
}
