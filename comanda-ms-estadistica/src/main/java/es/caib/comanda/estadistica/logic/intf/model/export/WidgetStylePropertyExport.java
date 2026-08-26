package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetStylePropertyExport {

    @NotNull
    private WidgetStyleScope scope;

    @NotBlank
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
