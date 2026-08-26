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
    @Size(max = es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity.PROPERTY_NAME_MAX_LENGTH)
    private String propertyName;
    @NotNull
    private WidgetStyleValueType valueType;
    private PaletteRole paletteRole;
    private Integer paletteIndex;
    @Size(max = es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity.SCALAR_VALUE_MAX_LENGTH)
    private String scalarValue;
    private Boolean defaultProperty;
    private Integer ordre;
}
