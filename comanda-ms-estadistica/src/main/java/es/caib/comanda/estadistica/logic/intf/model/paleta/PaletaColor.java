package es.caib.comanda.estadistica.logic.intf.model.paleta;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaletaColor extends BaseResource<Long> {

    protected ResourceReference<Paleta, Long> paleta;
    @NotNull
    private Integer posicio;
    @NotNull
    @Size(max = 64)
    private String valor;
}
