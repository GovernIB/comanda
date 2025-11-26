package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "IndicadorDesc", description = "Descripció d'un indicador/mesura disponible")
public class IndicadorDesc {
    @Schema(description = "Codi de l'indicador", example = "visites")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Nom de l'indicador", example = "Nombre de visites")
    @NotNull @Size(min = 1, max = 64)
    private String nom;
    @Schema(description = "Descripció funcional de l'indicador", example = "Total de visites registrades per període")
    private String descripcio;
    @Schema(description = "Format de representació del valor de l'indicador")
    @Valid
    private Format format;
}
