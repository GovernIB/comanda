package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaExport implements Serializable {

    private String nom;

    private List<PlantillaGrupPaletesExport> paletteGroups;
    private List<WidgetStylePropertyExport> styleProperties;

}
