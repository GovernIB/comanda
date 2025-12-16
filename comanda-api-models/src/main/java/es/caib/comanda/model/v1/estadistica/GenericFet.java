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
@Schema(name = "GenericFet", description = "Implementació genèrica d'un fet/indicador (codi, valor)")
public class GenericFet implements Fet {
    @Schema(description = "Codi del fet o indicador", example = "visites")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Valor numèric associat al codi")
    @NotNull
    private Double valor;
}
