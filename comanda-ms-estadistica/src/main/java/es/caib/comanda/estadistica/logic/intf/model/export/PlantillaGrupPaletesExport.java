package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGrupPaletesExport {
    private PaletteGroupType groupType;
    private PaletaExport widgetPalette;
    private PaletaExport chartPalette;
    private Integer ordre;
}
