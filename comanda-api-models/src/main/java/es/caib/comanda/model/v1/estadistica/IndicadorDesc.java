package es.caib.comanda.model.v1.estadistica;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "IndicadorDesc", description = "Descripció d'un indicador/mesura disponible")
public class IndicadorDesc {
    @Schema(description = "Codi de l'indicador", example = "NUM_VIS")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Nom de l'indicador", example = "Nombre de visites")
    @NotNull @Size(min = 1, max = 64)
    private String nom;
    @Schema(description = "Descripció funcional de l'indicador", example = "Total de visites registrades per període")
    @Size(max = 1024)
    private String descripcio;
    @Schema(description = "Format de representació del valor de l'indicador", example = "LONG")
    @Valid
    private Format format;
}
