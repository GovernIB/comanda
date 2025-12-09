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
@Schema(name = "IntegracioInfo", description = "Informació d'una integració exposada per l'aplicació")
public class IntegracioInfo {
    @Schema(description = "Codi identificador de la integració", example = "NOTIB")
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @Schema(description = "Nom descriptiu de la integració", example = "NOTIB - Notificacions")
    @NotNull @Size(min = 1, max = 255)
    private String nom;

    // Custom builder
    public static class IntegracioInfoBuilder {

        public IntegracioInfoBuilder integracioApp(IntegracioApp app) {
            this.codi = app.name();
            this.nom = app.getNom();
            return this;
        }

    }
}
