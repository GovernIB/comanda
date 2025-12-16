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
@Schema(name = "SubsistemaInfo", description = "Informació d'un subsistema intern de l'aplicació")
public class SubsistemaInfo {
    @Schema(description = "Codi del subsistema", example = "ALTA_REST")
    @NotNull @Size(min = 1, max = 64)
    private String codi;
    @Schema(description = "Nom del subsistema", example = "Alta de elements via REST")
    @NotNull @Size(min = 1, max = 255)
    private String nom;
}
