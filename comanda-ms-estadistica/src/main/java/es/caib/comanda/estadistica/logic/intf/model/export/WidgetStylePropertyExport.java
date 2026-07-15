package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetStylePropertyExport {

    private WidgetStyleScope scope;
    private String propertyName;
    private WidgetStyleValueType valueType;
    private PaletteRole paletteRole;
    private Integer paletteIndex;
    private String scalarValue;
    private Boolean defaultProperty;
    private Integer ordre;

}
