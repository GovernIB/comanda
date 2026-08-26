package es.caib.comanda.model.v1.estadistica;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EntitatDesc", description = "Descripció d'una entitat disponible")
public class EntitatDesc {
    @Schema(description = "Codi de la entitat", example = "GOIB")
    @NotNull
    @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Nom de la entitat", example = "Govern de les Illes Balears")
    @NotNull
    @Size(min = 1, max = 255)
    private String nom;
    @Schema(description = "Codi Dir3 de la entitat", example = "A04003003")
    @NotNull
    @Size(min = 1, max = 64)
    private String codiDir3;
    @Schema(description = "CIF de la entitat", example = "Entitat")
    @NotNull
    @Size(min = 1, max = 16)
    private String cif;
}
