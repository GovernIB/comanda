package es.caib.comanda.model.v1.permis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Objecte", description = "Objecte funcional sobre el qual s'apliquen permisos")
public class Objecte implements Serializable {

    @Schema(description = "Tipus d'objecte", example = "EXPEDIENT")
    private String tipus;

    @Schema(description = "Nom o títol de l'objecte", example = "EXP-12345/2025")
    private String nom;

    @Schema(description = "Identificador únic de l'objecte", example = "12345")
    private String identificador;

}
