package es.caib.comanda.model.v1.salut;

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
@Schema(name = "DetallSalut", description = "Detall específic d'estat de salut (parella clau-valor)")
public class DetallSalut {
    @Schema(description = "Codi del detall", example = "latencia-db")
    @NotNull @Size(min = 1, max = 10)
    private String codi;
    @Schema(description = "Nom del detall", example = "Latència BD")
    @NotNull @Size(min = 1, max = 100)
    private String nom;
    @Schema(description = "Valor del detall", example = "120ms")
    @NotNull @Size(min = 1, max = 1024)
    private String valor;
}
