package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "GenericFet", description = "Implementació genèrica d'un fet/indicador (codi, valor)")
public class GenericFet implements Fet {
    @Schema(description = "Codi del fet o indicador", example = "visites")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Valor numèric associat al codi")
    @NotNull
    private Double valor;
}
