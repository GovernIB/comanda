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
public class IndicadorTaula extends BaseResource<Long> {

    @NotNull
    private ResourceReference<Indicador, Long> indicador;
    @NotNull
    private ResourceReference<EstadisticaTaulaWidget, Long> widget;
    @NotNull
    private TableColumnsEnum tipus;
    @NotNull
    @Size(max = 64)
    private String titol;

}
