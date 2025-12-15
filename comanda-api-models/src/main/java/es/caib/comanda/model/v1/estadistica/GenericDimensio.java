package es.caib.comanda.model.v1.estadistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "GenericDimensio", description = "Implementació genèrica d'una dimensió (codi-valor)")
public class GenericDimensio implements Dimensio {
    @Schema(description = "Codi de la dimensió", example = "canal")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Valor de la dimensió", example = "WEB")
    private String valor;
}
