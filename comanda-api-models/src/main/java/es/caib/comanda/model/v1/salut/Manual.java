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
@Schema(name = "Manual", description = "Referència a un manual o documentació funcional")
public class Manual {
    @Schema(description = "Nom del manual", example = "Guia d'usuari")
    @NotNull @Size(min = 1, max = 128)
    private String nom;
    @Schema(description = "Ruta o URL del manual", example = "https://www.caib.es/docs/guia-usuari.pdf")
    @NotNull @Size(min = 1, max = 255)
    private String path;
}
