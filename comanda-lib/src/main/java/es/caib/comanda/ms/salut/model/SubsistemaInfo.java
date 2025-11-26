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
@Schema(name = "SubsistemaInfo", description = "Informació d'un subsistema intern de l'aplicació")
public class SubsistemaInfo {
    @Schema(description = "Codi del subsistema", example = "BD")
    @NotNull @Size(min = 1)
    private String codi;
    @Schema(description = "Nom del subsistema", example = "Base de Dades")
    @NotNull @Size(min = 1)
    private String nom;
}
