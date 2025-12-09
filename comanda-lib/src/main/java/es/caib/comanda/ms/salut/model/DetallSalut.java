package es.caib.comanda.ms.salut.model;

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
