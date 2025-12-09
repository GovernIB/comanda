package es.caib.comanda.ms.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Permis", description = "Sol·licitud de permisos per a un usuari o grup sobre un objecte")
public class Permis implements Serializable {

    @Schema(description = "Codi de l'aplicació que sol·licita el permís", example = "PORTAFIB")
    private String appCodi;

    @Schema(description = "Codi de l'entorn de l'aplicació", example = "DEV")
    private String entornCodi;

    @Schema(description = "Usuari al qual s'aplica el permís")
    private Usuari usuari;

    @Schema(description = "Grup al qual s'aplica el permís", example = "GESTORS")
    private String grup;

    @Schema(description = "Llista de permisos sol·licitats", example = "[\"LECTURA\", \"ESCRIPTURA\"]")
    private List<String> permisos;

    @Schema(description = "Objecte sobre el qual s'atorguen els permisos")
    private Objecte objecte;

    @Schema(description = "Objectes hereus als quals també s'apliquen els permisos")
    private List<Objecte> objectesHereus;

}
