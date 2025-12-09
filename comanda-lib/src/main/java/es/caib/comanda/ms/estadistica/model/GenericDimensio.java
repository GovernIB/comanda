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
@Schema(name = "GenericDimensio", description = "Implementació genèrica d'una dimensió (codi-valor)")
public class GenericDimensio implements Dimensio {
    @Schema(description = "Codi de la dimensió", example = "canal")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Valor de la dimensió", example = "WEB")
    private String valor;
}
