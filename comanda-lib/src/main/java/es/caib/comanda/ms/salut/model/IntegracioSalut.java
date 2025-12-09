package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "IntegracioSalut", description = "Estat de salut d'una integració concreta")
public class IntegracioSalut extends EstatSalut {
    @Schema(description = "Codi de la integració", example = "NOTIB")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Mètriques de peticions associades a la integració")
    @Valid
    private IntegracioPeticions peticions;
}
