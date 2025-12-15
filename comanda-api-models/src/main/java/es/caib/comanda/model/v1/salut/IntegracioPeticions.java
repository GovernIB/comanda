package es.caib.comanda.model.v1.salut;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "IntegracioPeticions", description = "Mètriques de peticions d'una integració: totals i darrer període")
public class IntegracioPeticions {

    // Dades totals
    @Schema(description = "Nombre total de peticions amb resultat correcte")
    @NotNull
    private Long totalOk;
    @Schema(description = "Nombre total de peticions amb error")
    @NotNull
    private Long totalError;
    @Schema(description = "Temps mig total de resposta (ms)")
    @NotNull
    private Integer totalTempsMig;

    // Dades per període consultat
    @Schema(description = "Peticions OK en el darrer període")
    @NotNull
    private Long peticionsOkUltimPeriode;
    @Schema(description = "Peticions en error en el darrer període")
    @NotNull
    private Long peticionsErrorUltimPeriode;
    @Schema(description = "Temps mig de resposta en el darrer període (ms)")
    @NotNull
    private Integer tempsMigUltimPeriode;

    @Schema(description = "Endpoint concret associat a aquestes mètriques", example = "https://path/to/integracio")
    @Size(max = 255)
    private String endpoint;


    @Schema(
            description = "Mètriques per entorn (clau = codi d'entorn)",
            example = "{\"ENT1\":{\"totalOk\":0,\"totalError\":0,\"totalTempsMig\":0,\"peticionsOkUltimPeriode\":0,\"peticionsErrorUltimPeriode\":0,\"tempsMigUltimPeriode\":0,\"endpoint\":\"https://path/to/entorn1\"},\"ENT2\":{\"totalOk\":0,\"totalError\":0,\"totalTempsMig\":0,\"peticionsOkUltimPeriode\":0,\"peticionsErrorUltimPeriode\":0,\"tempsMigUltimPeriode\":0,\"endpoint\":\"https://path/to/entorn2\"}}"
    )
    @Builder.Default
    private Map<String, IntegracioPeticions> peticionsPerEntorn = new HashMap<>();

    @AssertTrue(message = "No pot existir cap entrada amb codi (clau) null a peticionsPerEntorn")
    private boolean isPeticionsPerEntornSenseClausNulles() {
        return peticionsPerEntorn == null || !(peticionsPerEntorn.containsKey(null) || peticionsPerEntorn.containsKey(""));
    }
}
