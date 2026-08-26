package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaExport implements Serializable {

    @NotBlank
    @Size(max = es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity.NOM_MAX_LENGTH)
    private String nom;

    @Valid
    private List<PlantillaGrupPaletesExport> paletteGroups;

    @Valid
    private List<WidgetStylePropertyExport> styleProperties;

}
