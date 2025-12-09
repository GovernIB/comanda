package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SubsistemaSalut", description = "Estat de salut i mètriques d'un subsistema intern")
public class SubsistemaSalut extends EstatSalut {
    @Schema(description = "Codi del subsistema", example = "BD")
    @NotNull @Size(min = 1, max = 32)
    private String codi;

    // Dades totals
    @Schema(description = "Total de peticions amb resultat correcte")
    @NotNull
    private Long totalOk;
    @Schema(description = "Total de peticions amb error")
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
}
