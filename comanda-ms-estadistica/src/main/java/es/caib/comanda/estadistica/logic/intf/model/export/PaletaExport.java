package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaletaExport {

    @NotBlank
    @Size(max = es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity.NOM_MAX_LENGTH)
    private String nom;

    @Size(max = es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity.DESCRIPCIO_MAX_LENGTH)
    private String descripcio;

    private String colors;

}
