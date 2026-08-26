package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaGrupPaletesExport {
    @NotNull
    private PaletteGroupType groupType;

    @Valid
    private PaletaExport widgetPalette;

    @Valid
    private PaletaExport chartPalette;

    private Integer ordre;
}
