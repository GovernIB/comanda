package es.caib.comanda.estadistica.logic.intf.model.paleta;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
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
public class WidgetStyleProperty extends BaseResource<Long> {

    @NotNull
    private WidgetStyleScope scope;
    @NotNull
    @Size(max = 64)
    private String propertyName;
    @Size(max = 128)
    private String label;
    @NotNull
    private WidgetStyleValueType valueType;
    private PaletteRole paletteRole;
    private Integer paletteIndex;
    @Size(max = 1000)
    private String scalarValue;
    private Boolean defaultProperty;
    private Integer ordre;
}
