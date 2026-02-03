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
@Schema(name = "IntegracioInfo", description = "Informació d'una integració exposada per l'aplicació")
public class IntegracioInfo {
    @Schema(description = "Codi identificador de la integració", example = "REG")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Nom descriptiu de la integració", example = "Registre")
    @NotNull @Size(min = 1, max = 255)
    private String nom;

}
