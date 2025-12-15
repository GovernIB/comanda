package es.caib.comanda.model.v1.salut;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "IntegracioSalut", description = "Estat de salut d'una integració concreta")
public class IntegracioSalut extends EstatSalut {
    @Schema(description = "Codi de la integració", example = "REG")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Mètriques de peticions associades a la integració")
    @Valid
    private IntegracioPeticions peticions;
}
