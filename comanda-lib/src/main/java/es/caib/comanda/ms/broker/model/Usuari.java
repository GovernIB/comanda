package es.caib.comanda.ms.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Usuari", description = "Dades bàsiques d'un usuari")
public class Usuari implements Serializable {

    @Schema(description = "Codi únic de l'usuari", example = "usr1234")
    private String codi;

    @Schema(description = "Nom complet de l'usuari", example = "Anna Serra")
    private String nom;

    @Schema(description = "NIF/NIE de l'usuari", example = "12345678Z")
    private String nif;

    @Schema(description = "Correu electrònic", example = "anna.serra@example.org", format = "email")
    private String email;

}
